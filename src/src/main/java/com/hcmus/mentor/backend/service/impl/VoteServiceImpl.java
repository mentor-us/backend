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
import java.util.Optional;

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

        VoteDetailResponse voteDetail = fulfillChoices(vote);
        voteDetail.setCanEdit(group.isMentor(userId) || vote.getCreator().getId().equals(userId));
        return voteDetail;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoteDetailResponse> getGroupVotes(String userId, String channelId) {
        if (!permissionService.isUserIdInGroup(userId, channelId)) {
            return null;
        }
        return voteRepository.findByGroupIdOrderByCreatedDateDesc(channelId).stream()
                .map(this::fulfillChoices)
                .toList();
    }


    @Override
    public VoteDetailResponse fulfillChoices(Vote vote) {
//        ShortProfile creator = new ShortProfile(vote.getCreator());
//        List<VoteDetailResponse.ChoiceDetail> choices = vote.getChoices().stream()
//                .map(this::fulfillChoice)
//                .filter(Objects::nonNull)
//                .sorted((c1, c2) -> c2.getVoters().size() - c1.getVoters().size())
//                .toList();
//        return VoteDetailResponse.from(vote, creator, choices);
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
        var channel = channelRepository.findById(request.getGroupId()).orElseThrow(()-> new DomainException("Không tìm thấy kênh"));
        var group = channel.getGroup();

        if(!group.isMember(userId)) {
            throw new ForbiddenException("Người dùng không có trong channel!");
        }

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

        var user = userRepository.findById(userDetails.getId()).orElseThrow(() -> new DomainException("Không tìm thấy người dùng."));

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
            return false;
        }
        if (!permissionService.isMentor(user.getEmail(), vote.getCreator().getId())
                && !vote.getGroup().getId().equals(user.getId())) {
            return false;
        }
        voteRepository.delete(vote);
        return true;
    }

    @Override
    public Vote doVoting(DoVotingRequest request) {
        var vote = voteRepository.findById(request.getVoteId()).orElse(null);
        if (vote == null) {
            return null;
        }

        if (Vote.Status.CLOSED.equals(vote.getStatus())) {
            return null;
        }

        var voter = userRepository.findById(request.getVoterId()).orElse(null);
        if (voter == null) {
            return null;
        }

        List<Choice> newChoices = request.getChoices().stream()
                .map(choice -> vote.getChoice(choice.getId()))
                .toList();

        if (!newChoices.isEmpty()) {
            choiceRepository.saveAll(newChoices);
        }

        return voteRepository.save(vote);
    }

    @Override
    public VoteDetailResponse.ChoiceDetail getChoiceDetail(
            CustomerUserDetails user, String voteId, String choiceId) {
        Optional<Vote> voteWrapper = voteRepository.findById(voteId);
        if (!voteWrapper.isPresent()) {
            return null;
        }
        Vote vote = voteWrapper.get();
        if (!permissionService.isUserIdInGroup(user.getId(), vote.getGroup().getId())) {
            return null;
        }

        return fulfillChoice(vote.getChoice(choiceId));
    }

    @Override
    public List<VoteDetailResponse.ChoiceDetail> getChoiceResults(CustomerUserDetails user, String voteId) {
        Optional<Vote> voteWrapper = voteRepository.findById(voteId);
        if (voteWrapper.isEmpty()) {
            return null;
        }
        Vote vote = voteWrapper.get();
        if (!permissionService.isUserIdInGroup(user.getId(), vote.getGroup().getId())) {
            return null;
        }

        VoteDetailResponse voteDetail = fulfillChoices(vote);
        if (voteDetail == null) {
            return null;
        }

        return voteDetail.getChoices();
    }

    @Override
    public boolean closeVote(CustomerUserDetails user, String voteId) {
        var vote = voteRepository.findById(voteId).orElse(null);
        if (vote == null) {
            return false;
        }

        if (!permissionService.isMentor(user.getEmail(), vote.getGroup().getId())
                && !vote.getGroup().getId().equals(user.getId())) {
            return false;
        }
        if (Vote.Status.CLOSED.equals(vote.getStatus())) {
            return false;
        }
        vote.close();
        voteRepository.save(vote);
        return true;
    }

    @Override
    public boolean reopenVote(CustomerUserDetails user, String voteId) {
        Optional<Vote> voteWrapper = voteRepository.findById(voteId);
        if (voteWrapper.isEmpty()) {
            return false;
        }
        Vote vote = voteWrapper.get();
        if (!permissionService.isMentor(user.getEmail(), vote.getGroup().getId())
                && !vote.getCreator().getId().equals(user.getId())) {
            return false;
        }
        if (Vote.Status.OPEN.equals(vote.getStatus())) {
            return false;
        }
        vote.reopen();
        voteRepository.save(vote);
        return true;
    }
}