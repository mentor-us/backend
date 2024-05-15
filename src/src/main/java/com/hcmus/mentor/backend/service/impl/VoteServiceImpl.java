package com.hcmus.mentor.backend.service.impl;

import com.corundumstudio.socketio.SocketIOServer;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.request.CreateVoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateVoteRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.controller.usecase.vote.common.VoteResult;
import com.hcmus.mentor.backend.domain.Choice;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.domain.Vote;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.ChoiceRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.repository.VoteRepository;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final GroupService groupService;
    private final PermissionService permissionService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MessageService messageService;
    private final ChannelRepository channelRepository;
    private final SocketIOServer socketServer;
    private final ChoiceRepository choiceRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public VoteDetailResponse get(String userId, String voteId) {
        var vote = voteRepository.findById(voteId).orElseThrow(() -> new DomainException("Không tìm thấy bình chọn."));
        Group group = vote.getGroup().getGroup();
        if (!group.isMember(userId)) {
            return null;
        }

        VoteDetailResponse voteDetail = modelMapper.map(vote, VoteDetailResponse.class);
        voteDetail.setCanEdit(group.isMentor(userId) || vote.getCreator().getId().equals(userId));
        return voteDetail;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoteDetailResponse> getGroupVotes(String userId, String channelId) {
        if (!permissionService.isMemberInGroup(userId, channelId)) {
            return null;
        }
        return voteRepository.findByGroupIdOrderByCreatedDateDesc(channelId).stream()
                .map(vote -> modelMapper.map(vote, VoteDetailResponse.class))
                .toList();
    }


    @Override
    public VoteDetailResponse fulfillChoices(Vote vote) {
        return modelMapper.map(vote, VoteDetailResponse.class);
    }

    @Override
    public VoteDetailResponse.ChoiceDetail fulfillChoice(Choice choice) {
        if (choice == null) {
            return null;
        }

        List<ShortProfile> voters = choice.getVoters().stream().map(user -> modelMapper.map(user, ShortProfile.class)).toList();
        return VoteDetailResponse.ChoiceDetail.from(choice, voters);
    }

    @Override
    @Transactional
    public VoteResult createNewVote(String userId, CreateVoteRequest request) {
        var sender = userRepository.findById(userId).orElseThrow(() -> new DomainException("User not found."));
        if(!permissionService.isMemberInChannel(userId, request.getGroupId())) {
            throw new ForbiddenException("user not in channel");
        }

        var channel = channelRepository.findById(request.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));

        Vote newVote = voteRepository.save(Vote.builder()
                .question(request.getQuestion())
                .group(channel)
                .creator(sender)
                .timeEnd(request.getTimeEnd())
                .isMultipleChoice(request.getIsMultipleChoice())
                .build());
        var choices = request.getChoices().stream()
                .map(choice -> Choice.builder()
                        .name(choice.getName())
                        .creator(sender)
                        .vote(newVote)
                        .build())
                .toList();
        choiceRepository.saveAll(choices);

        request.setCreatorId(userId);


        Message message = messageService.saveVoteMessage(newVote);
        MessageDetailResponse response = MessageDetailResponse.from(MessageResponse.from(message, ProfileResponse.from(sender)), newVote);
        socketServer.getRoomOperations(request.getGroupId()).sendEvent("receive_message", response);

        notificationService.sendNewVoteNotification(newVote.getGroup().getId(), newVote);
        groupService.pingGroup(newVote.getGroup().getId());
        return modelMapper.map(newVote, VoteResult.class);
    }

    @Override
    public boolean updateVote(CustomerUserDetails userDetails, String voteId, UpdateVoteRequest request) {
        var user = userRepository.findById(userDetails.getId()).orElseThrow(() -> new DomainException("Không tìm thấy người dùng."));
        var vote = voteRepository.findById(voteId).orElseThrow(() -> new DomainException("Không tìm thấy bình chọn."));

        if (vote.getGroup().getGroup().isMentor(userDetails.getId())
                || vote.getCreator().getId().equals(userDetails.getId())) {
            throw new ForbiddenException("Không có quyền cập nhật bình chọn.");
        }

        var isUpdate = false;

        if (request.getQuestion() != null) {
            vote.setQuestion(request.getQuestion());
            isUpdate = true;
        }

        if (request.getTimeEnd() != null) {
            vote.setTimeEnd(request.getTimeEnd());
            isUpdate = true;
        }


        if (request.getChoices() != null) {
            List<Choice> newChoices = new ArrayList<>();
            for (var choiceDto : request.getChoices()) {
                var choice = vote.getChoice(choiceDto.getId());
                if (choice == null) {
                    var voters = choiceDto.getVoters().stream().map(voterId -> userRepository.findById(voterId).orElseThrow(() -> new DomainException("Không tìm thấy người dùng."))).toList();
                    var newChoice = Choice.builder().name(choiceDto.getName()).creator(user).voters(voters).vote(vote).build();
                    newChoices.add(newChoice);
                } else {
                    newChoices.add(choice);
                }
            }

            if (!newChoices.isEmpty()) {
                choiceRepository.saveAll(newChoices);
                isUpdate = true;
            }
        }

        if (isUpdate) {
            voteRepository.save(vote);
        }
        return isUpdate;
    }

    @Override
    public boolean deleteVote(CustomerUserDetails user, String voteId) {
        var vote = voteRepository.findById(voteId).orElse(null);
        if (vote == null) {
            throw new DomainException("Không tìm thấy bình chọn");
        }

        if (permissionService.isMentorInChannel(user.getId(), vote.getGroup().getId())) {
            throw new ForbiddenException("Bạn không có quyền truy cập bình chọn");
        }
        voteRepository.delete(vote);
        return true;
    }

    @Override
    public Vote doVoting(DoVotingRequest request, String userId) {
        var vote = voteRepository.findById(request.getVoteId()).orElse(null);
        if (vote == null) {
            throw new DomainException("Không tìm thấy bình chọn!");
        }
        if (vote.getStatus().equals(Vote.Status.CLOSED)) {
            throw new DomainException("Bình chọn đang đóng");
        }

        List<Choice> doChoices = request.getChoices().stream()
                .map(newChoiceDto -> {
                    var choice = vote.getChoice(newChoiceDto.getId());
                    if (choice == null) {
                        choice = Choice.builder()
                                .vote(vote)
                                .creator(userRepository.findById(userId).orElse(null))
                                .name(newChoiceDto.getName())
                                .build();
                        choice.setId(newChoiceDto.getId());
                    }

                    var voters = userRepository.findAllByIdIn(newChoiceDto.getVoters());
                    choice.setVoters(voters);

                    return choice;
                }).toList();

        choiceRepository.saveAll(doChoices);
        return voteRepository.save(vote);
    }

    @Override
    public VoteDetailResponse.ChoiceDetail getChoiceDetail(CustomerUserDetails user, String voteId, String choiceId) {
        var vote = voteRepository.findById(voteId).orElse(null);
        if (vote == null) {
            throw new DomainException("Không tìm thấy bình chọn");
        }

        if (permissionService.isMemberInChannel(vote.getGroup().getId(), user.getId())) {
            throw new ForbiddenException("Bạn không có quyền truy cập bình chọn");
        }

        if (vote.getChoice(choiceId) == null) {
            throw new DomainException("Không tìm thấy lựa chọn");
        }

        return modelMapper.map(vote.getChoice(choiceId), VoteDetailResponse.ChoiceDetail.class);
    }

    @Override
    public List<VoteDetailResponse.ChoiceDetail> getChoiceResults(CustomerUserDetails user, String voteId) {
        var vote = voteRepository.findById(voteId).orElse(null);
        if (vote == null) {
            throw new DomainException("Không tìm thấy bình chọn");
        }

        if (permissionService.isMemberInChannel(vote.getGroup().getId(), user.getId())) {
            throw new ForbiddenException("Bạn không có quyền truy cập bình chọn");
        }

        VoteDetailResponse voteDetail = modelMapper.map(vote, VoteDetailResponse.class);
        return voteDetail.getChoices();
    }

    @Override
    public void closeVote(CustomerUserDetails user, String voteId) {
        var vote = voteRepository.findById(voteId).orElse(null);
        if (vote == null) {
            throw new DomainException("Không tìm thấy bình chọn!");
        }
        if (vote.getStatus().equals(Vote.Status.CLOSED)) {
            throw new DomainException("Bình chọn đang đóng");
        }

        if (!permissionService.isMentorInChannel(vote.getGroup().getId(), user.getId()) && !Objects.equals(vote.getCreator().getId(), user.getId())) {
            throw new ForbiddenException("Bạn không có quyền truy cập bình chọn");
        }

        vote.close();
        voteRepository.save(vote);
    }

    @Override
    public void reopenVote(CustomerUserDetails user, String voteId) {
        var vote = voteRepository.findById(voteId).orElse(null);
        if (vote == null) {
            throw new DomainException("Không tìm thấy bình chọn");
        }
        if (vote.getStatus().equals(Vote.Status.OPEN)) {
            throw new DomainException("Bình chọn đang mở");
        }

        if (!permissionService.isMentorInChannel(vote.getGroup().getId(), user.getId()) && !Objects.equals(vote.getCreator().getId(), user.getId())) {
            throw new ForbiddenException("Bạn không có quyền truy cập bình chọn");
        }

        vote.close();
        voteRepository.save(vote);
    }
}