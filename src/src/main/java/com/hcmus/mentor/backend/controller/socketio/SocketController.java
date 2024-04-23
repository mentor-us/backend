package com.hcmus.mentor.backend.controller.socketio;

import an.awesome.pipelinr.Pipeline;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.hcmus.mentor.backend.controller.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.controller.payload.request.JoinOutRoomRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.controller.usecase.channel.updatelastmessage.UpdateLastMessageCommand;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.Vote;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.*;
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

    private final Pipeline pipeline;

    public SocketController(
            SocketIOServer server,
            SocketIOService socketIOService,
            UserRepository userRepository,
            MessageService messageService,
            NotificationService notificationService,
            VoteService voteService,
            Pipeline pipeline) {
        this.server = server;
        this.socketIOService = socketIOService;
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.notificationService = notificationService;
        this.voteService = voteService;
        this.pipeline = pipeline;
        configureServer(this.server);
    }

    private void configureServer(SocketIOServer server) {
        server.addEventListener("join_room", JoinOutRoomRequest.class, onJoinRoom());
        server.addEventListener("out_room", JoinOutRoomRequest.class, onOutRoom());
        server.addEventListener("send_message", Message.class, onChatReceived());
        server.addEventListener("send_voting", DoVotingRequest.class, onVotingReceived());

        LOGGER.info("[*] Configure Socket IO Server listener.");
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
            User user = userRepository.findById(message.getSender().getId()).orElse(null);

            MessageDetailResponse response = MessageDetailResponse.from(message, user);
            MessageResponse buffer = MessageResponse.from(message, ProfileResponse.from(user));
            if (message.getReply() != null) {
                response = messageService.fulfillTextMessage(buffer);
            }

            socketIOService.sendMessage(socketIOClient, response, newMessage.getChannel().getGroup().getId());
            notificationService.sendNewMessageNotification(response);

            var updateLastMessageCommand = UpdateLastMessageCommand.builder()
                    .message(newMessage)
                    .channel(message.getChannel()).build();
            pipeline.send(updateLastMessageCommand);
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