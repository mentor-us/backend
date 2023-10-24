package com.hcmus.mentor.backend.web.controller.socketio;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.hcmus.mentor.backend.entity.Message;
import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.entity.Vote;
import com.hcmus.mentor.backend.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.payload.request.JoinOutRoomRequest;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.usercase.common.repository.UserRepository;
import com.hcmus.mentor.backend.usercase.common.repository.VoteRepository;
import com.hcmus.mentor.backend.usercase.common.service.GroupService;
import com.hcmus.mentor.backend.usercase.common.service.MessageService;
import com.hcmus.mentor.backend.usercase.common.service.NotificationService;
import com.hcmus.mentor.backend.usercase.common.service.SocketIOService;
import com.hcmus.mentor.backend.usercase.common.service.VoteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class SocketController {

  private static final Logger LOGGER = LogManager.getLogger(SocketController.class);

  private final SocketIOServer server;

  private final SocketIOService socketIOService;

  private final UserRepository userRepository;

  private final MessageService messageService;

  private final NotificationService notificationService;

  private final VoteService voteService;

  private final VoteRepository voteRepository;

  private final GroupService groupService;

  public SocketController(
      SocketIOServer server,
      SocketIOService socketIOService,
      UserRepository userRepository,
      MessageService messageService,
      NotificationService notificationService,
      VoteService voteService,
      VoteRepository voteRepository,
      GroupService groupService) {
    this.server = server;
    this.socketIOService = socketIOService;
    this.userRepository = userRepository;
    this.messageService = messageService;
    this.notificationService = notificationService;
    this.voteService = voteService;
    this.voteRepository = voteRepository;
    this.groupService = groupService;
    configureServer(this.server);
  }

  private void configureServer(SocketIOServer server) {
    server.addConnectListener(onConnected());
    server.addDisconnectListener(onDisconnected());
    server.addEventListener("join_room", JoinOutRoomRequest.class, onJoinRoom());
    server.addEventListener("out_room", JoinOutRoomRequest.class, onOutRoom());
    server.addEventListener("send_message", Message.class, onChatReceived());
    server.addEventListener("send_voting", DoVotingRequest.class, onVotingReceived());

    LOGGER.info("[*] Configure Socket IO Server listener.");
  }

  private ConnectListener onConnected() {
    return client ->
        client.sendEvent("receive_message", Message.builder().content("Hello from server").build());
  }

  private DisconnectListener onDisconnected() {
    return client -> {};
  }

  private DataListener<JoinOutRoomRequest> onJoinRoom() {
    return (client, payload, ackRequest) -> {
      if (payload.getGroupId() == null || payload.getGroupId().isEmpty()) {
        return;
      }
      client.joinRoom(payload.getGroupId());
    };
  }

  private DataListener<JoinOutRoomRequest> onOutRoom() {
    return (client, payload, ackRequest) -> {
      if (payload.getGroupId() == null || payload.getGroupId().isEmpty()) {
        return;
      }
      client.leaveRoom(payload.getGroupId());
    };
  }

  private DataListener<Message> onChatReceived() {
    return (socketIOClient, message, ackRequest) -> {
      Message newMessage = messageService.saveMessage(message);
      User user = userRepository.findById(message.getSenderId()).orElse(null);

      MessageDetailResponse response = MessageDetailResponse.from(message, user);
      MessageResponse buffer = MessageResponse.from(message, ProfileResponse.from(user));
      if (message.getReply() != null) {
        response = messageService.fulfillTextMessage(buffer);
      }
      socketIOService.sendMessage(socketIOClient, response, newMessage.getGroupId());
      notificationService.sendNewMessageNotification(response);
      groupService.updateLastMessageId(message.getGroupId(), message.getId());
    };
  }

  private DataListener<DoVotingRequest> onVotingReceived() {
    return (socketIOClient, doVoting, ackRequest) -> {
      Vote updatedVote = voteService.doVoting(doVoting);
      VoteDetailResponse response = voteService.fulfillChoices(updatedVote);
      socketIOService.sendUpdatedVote(socketIOClient, response);
    };
  }
}
