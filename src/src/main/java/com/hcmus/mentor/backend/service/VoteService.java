package com.hcmus.mentor.backend.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.Message;
import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.entity.Vote;
import com.hcmus.mentor.backend.payload.request.CreateVoteRequest;
import com.hcmus.mentor.backend.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.payload.request.UpdateVoteRequest;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.repository.VoteRepository;
import com.hcmus.mentor.backend.security.UserPrincipal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class VoteService {

  private final VoteRepository voteRepository;

  private final GroupService groupService;

  private final GroupRepository groupRepository;

  private final PermissionService permissionService;

  private final UserRepository userRepository;

  private final NotificationService notificationService;

  private final MessageService messageService;

  private final SocketIOServer socketServer;

  public VoteService(
      VoteRepository voteRepository,
      @Lazy GroupService groupService,
      GroupRepository groupRepository,
      PermissionService permissionService,
      UserRepository userRepository,
      NotificationService notificationService,
      @Lazy MessageService messageService,
      SocketIOServer socketServer) {
    this.voteRepository = voteRepository;
    this.groupService = groupService;
    this.groupRepository = groupRepository;
    this.permissionService = permissionService;
    this.userRepository = userRepository;
    this.notificationService = notificationService;
    this.messageService = messageService;
    this.socketServer = socketServer;
  }

  public VoteDetailResponse get(String userId, String voteId) {
    Optional<Vote> voteWrapper = voteRepository.findById(voteId);
    if (!voteWrapper.isPresent()) {
      return null;
    }
    Vote vote = voteWrapper.get();
    if (!permissionService.isUserIdInGroup(userId, vote.getGroupId())) {
      return null;
    }

    Optional<Group> groupWrapper = groupRepository.findById(vote.getGroupId());
    if (!groupWrapper.isPresent()) {
      return null;
    }
    Group group = groupWrapper.get();

    VoteDetailResponse voteDetail = fulfillChoices(vote);
    voteDetail.setCanEdit(group.isMentor(userId) || vote.getCreatorId().equals(userId));
    return voteDetail;
  }

  public List<VoteDetailResponse> getGroupVotes(String userId, String groupId) {
    if (!permissionService.isUserIdInGroup(userId, groupId)) {
      return null;
    }
    return voteRepository.findByGroupIdOrderByCreatedDateDesc(groupId).stream()
        .map(this::fulfillChoices)
        .collect(Collectors.toList());
  }

  public VoteDetailResponse fulfillChoices(Vote vote) {
    ShortProfile creator = userRepository.findShortProfile(vote.getCreatorId());
    List<VoteDetailResponse.ChoiceDetail> choices =
        vote.getChoices().stream()
            .map(this::fulfillChoice)
            .filter(Objects::nonNull)
            .sorted((c1, c2) -> c2.getVoters().size() - c1.getVoters().size())
            .collect(Collectors.toList());
    return VoteDetailResponse.from(vote, creator, choices);
  }

  public VoteDetailResponse.ChoiceDetail fulfillChoice(Vote.Choice choice) {
    if (choice == null) {
      return null;
    }
    List<ShortProfile> voters = userRepository.findByIds(choice.getVoters());
    return VoteDetailResponse.ChoiceDetail.from(choice, voters);
  }

  public Vote createNewVote(String userId, CreateVoteRequest request) {
    if (!permissionService.isUserIdInGroup(userId, request.getGroupId())) {
      return null;
    }
    request.setCreatorId(userId);
    Vote newVote = voteRepository.save(Vote.from(request));

    Message message = messageService.saveVoteMessage(newVote);
    User sender = userRepository.findById(message.getSenderId()).orElse(null);
    MessageDetailResponse response =
        MessageDetailResponse.from(
            MessageResponse.from(message, ProfileResponse.from(sender)), newVote);
    socketServer.getRoomOperations(request.getGroupId()).sendEvent("receive_message", response);

    notificationService.sendNewVoteNotification(newVote.getCreatorId(), newVote);
    groupService.pingGroup(newVote.getGroupId());
    return newVote;
  }

  public boolean updateVote(UserPrincipal user, String voteId, UpdateVoteRequest request) {
    Optional<Vote> voteWrapper = voteRepository.findById(voteId);
    if (!voteWrapper.isPresent()) {
      return false;
    }
    Vote vote = voteWrapper.get();
    if (!permissionService.isMentor(user.getEmail(), vote.getGroupId())
        && !vote.getCreatorId().equals(user.getId())) {
      return false;
    }

    vote.update(request);
    voteRepository.save(vote);
    return true;
  }

  public boolean deleteVote(UserPrincipal user, String voteId) {
    Optional<Vote> voteWrapper = voteRepository.findById(voteId);
    if (!voteWrapper.isPresent()) {
      return false;
    }
    Vote vote = voteWrapper.get();
    if (!permissionService.isMentor(user.getEmail(), vote.getGroupId())
        && !vote.getCreatorId().equals(user.getId())) {
      return false;
    }
    voteRepository.delete(vote);
    return true;
  }

  public Vote doVoting(DoVotingRequest request) {
    Optional<Vote> voteWrapper = voteRepository.findById(request.getVoteId());
    if (!voteWrapper.isPresent()) {
      return null;
    }
    Vote vote = voteWrapper.get();
    if (Vote.Status.CLOSED.equals(vote.getStatus())) {
      return null;
    }
    vote.doVoting(request);
    return voteRepository.save(vote);
  }

  public VoteDetailResponse.ChoiceDetail getChoiceDetail(
      UserPrincipal user, String voteId, String choiceId) {
    Optional<Vote> voteWrapper = voteRepository.findById(voteId);
    if (!voteWrapper.isPresent()) {
      return null;
    }
    Vote vote = voteWrapper.get();
    if (!permissionService.isUserIdInGroup(user.getId(), vote.getGroupId())) {
      return null;
    }

    return fulfillChoice(vote.getChoice(choiceId));
  }

  public List<VoteDetailResponse.ChoiceDetail> getChoiceResults(UserPrincipal user, String voteId) {
    Optional<Vote> voteWrapper = voteRepository.findById(voteId);
    if (!voteWrapper.isPresent()) {
      return null;
    }
    Vote vote = voteWrapper.get();
    if (!permissionService.isUserIdInGroup(user.getId(), vote.getGroupId())) {
      return null;
    }

    VoteDetailResponse voteDetail = fulfillChoices(vote);
    if (voteDetail == null) {
      return null;
    }
    return voteDetail.getChoices();
  }

  public boolean closeVote(UserPrincipal user, String voteId) {
    Optional<Vote> voteWrapper = voteRepository.findById(voteId);
    if (!voteWrapper.isPresent()) {
      return false;
    }
    Vote vote = voteWrapper.get();
    if (!permissionService.isMentor(user.getEmail(), vote.getGroupId())
        && !vote.getCreatorId().equals(user.getId())) {
      return false;
    }
    if (Vote.Status.CLOSED.equals(vote.getStatus())) {
      return false;
    }
    vote.close();
    voteRepository.save(vote);
    return true;
  }

  public boolean reopenVote(UserPrincipal user, String voteId) {
    Optional<Vote> voteWrapper = voteRepository.findById(voteId);
    if (!voteWrapper.isPresent()) {
      return false;
    }
    Vote vote = voteWrapper.get();
    if (!permissionService.isMentor(user.getEmail(), vote.getGroupId())
        && !vote.getCreatorId().equals(user.getId())) {
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
