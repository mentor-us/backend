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
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @Override
    public VoteDetailResponse get(String userId, String voteId) {
        var vote = voteRepository.findById(voteId).orElseThrow(() -> new DomainException("Không tìm thấy bình chọn."));
        vote.getGroup().getGroup().isMentor(userId) ;
        if (!permissionService.isUserIdInGroup(userId, vote.getGroup().getId())) {
            return null;
        }

        Group group = vote.getGroup().getGroup();

        VoteDetailResponse voteDetail = fulfillChoices(vote);
        voteDetail.setCanEdit(group.isMentor(userId) || vote.getCreator().getId().equals(userId));
        return voteDetail;
    }

    @Override
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
        ShortProfile creator = userRepository.findShortProfile(vote.getCreator().getId());
        List<VoteDetailResponse.ChoiceDetail> choices = vote.getChoices().stream()
                .map(this::fulfillChoice)
                .filter(Objects::nonNull)
                .sorted((c1, c2) -> c2.getVoters().size() - c1.getVoters().size())
                .toList();
        return VoteDetailResponse.from(vote, creator, choices);
    }

    @Override
    public VoteDetailResponse.ChoiceDetail fulfillChoice(Choice choice) {
        if (choice == null) {
            return null;
        }

        List<ShortProfile> voters = choice.getVoters().stream().map(ShortProfile::new).toList();
        return VoteDetailResponse.ChoiceDetail.from(choice, voters);
    }

    @Override
    public Vote createNewVote(String userId, CreateVoteRequest request) {
        if (!permissionService.isUserInChannel(request.getGroupId(), userId)) {
            throw new DomainException("Người dùng không có trong channel!");
        }

        var choices = request.getChoices().stream()
                .map(choice -> Choice.builder().name(choice.getName()).build())
                .toList();

        var sender = userRepository.findById(userId).orElseThrow(() -> new DomainException("User not found."));
        var group = channelRepository.findById(request.getGroupId()).orElseThrow(() -> new DomainException("Channel not found."));

        request.setCreatorId(userId);
        Vote newVote = voteRepository.save(Vote.builder()
                .question(request.getQuestion())
                .group(group)
                .creator(sender)
                .timeEnd(request.getTimeEnd())
                .choices(choices)
                .isMultipleChoice(request.getIsMultipleChoice())
                .build());

        Message message = messageService.saveVoteMessage(newVote);
        MessageDetailResponse response = MessageDetailResponse.from(MessageResponse.from(message, ProfileResponse.from(sender)), newVote);
        socketServer.getRoomOperations(request.getGroupId()).sendEvent("receive_message", response);

        notificationService.sendNewVoteNotification(newVote.getGroup().getId(), newVote);
        groupService.pingGroup(newVote.getGroup().getId());
        return newVote;
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
        var vote = voteRepository.findById(request.getVoterId()).orElse(null);
        if(vote==null){
            return null;
        }

        if (Vote.Status.CLOSED.equals(vote.getStatus())) {
            return null;
        }

        var voter = userRepository.findById(request.getVoterId()).orElse(null);
        if(voter==null){
            return null;
        }

        var choices = vote.getChoices();

        List<Choice> newChoices = request.getChoices().stream().map(choiceDto -> {
            var choice = vote.getChoice(choiceDto.getId());
            if (choice == null) {
                var voters = choiceDto.getVoters().stream().map(voterId -> userRepository.findById(voterId).orElseThrow(() -> new DomainException("Không tìm thấy người dùng."))).toList();
                return Choice.builder().name(choiceDto.getName()).creator(voter).voters(voters).vote(vote).build();
            }
            return choice;
        }).toList();

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