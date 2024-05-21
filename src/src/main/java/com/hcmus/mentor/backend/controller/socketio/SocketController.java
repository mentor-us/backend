package com.hcmus.mentor.backend.controller.socketio;

import an.awesome.pipelinr.Pipeline;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.hcmus.mentor.backend.controller.payload.request.DoVotingRequest;
import com.hcmus.mentor.backend.controller.payload.request.JoinOutRoomRequest;
import com.hcmus.mentor.backend.controller.payload.request.ReceivedMessageRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.controller.usecase.channel.updatelastmessage.UpdateLastMessageCommand;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.domain.Vote;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.MessageService;
import com.hcmus.mentor.backend.service.NotificationService;
import com.hcmus.mentor.backend.service.SocketIOService;
import com.hcmus.mentor.backend.service.VoteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class SocketController {

    private static final Logger logger = LogManager.getLogger(SocketController.class);

    private final SocketIOServer server;

    private final SocketIOService socketIOService;

    private final UserRepository userRepository;

    private final MessageService messageService;

    private final VoteService voteService;

    private final ChannelRepository channelRepository;

    private final Pipeline pipeline;

    private final ModelMapper modelMapper;

    private final NotificationService notificationService;

    public SocketController(
            SocketIOServer server,
            SocketIOService socketIOService,
            UserRepository userRepository,
            MessageService messageService,
            VoteService voteService, ChannelRepository channelRepository,
            Pipeline pipeline, ModelMapper modelMapper, NotificationService notificationService) {
        this.server = server;
        this.socketIOService = socketIOService;
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.voteService = voteService;
        this.channelRepository = channelRepository;
        this.pipeline = pipeline;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
        configureServer(this.server);
    }

    private void configureServer(SocketIOServer server) {
        server.addEventListener("join_room", JoinOutRoomRequest.class, onJoinRoom());
        server.addEventListener("out_room", JoinOutRoomRequest.class, onOutRoom());
        server.addEventListener("send_message", ReceivedMessageRequest.class, onChatReceived());
        server.addEventListener("send_voting", DoVotingRequest.class, onVotingReceived());

        logger.info("[*] Configure Socket IO Server listener.");
    }

    private DataListener<JoinOutRoomRequest> onJoinRoom() {
        return (client, payload, ackRequest) -> {
            logger.info("Join room: {}", payload.getGroupId());

            if (payload.getGroupId() == null || payload.getGroupId().isEmpty()) {
                return;
            }
            client.joinRoom(payload.getGroupId());
        };
    }

    private DataListener<JoinOutRoomRequest> onOutRoom() {
        return (client, payload, ackRequest) -> {
            logger.info("Out room: {}", payload.getGroupId());

            if (payload.getGroupId() == null || payload.getGroupId().isEmpty()) {
                return;
            }
            client.leaveRoom(payload.getGroupId());
        };
    }

    private DataListener<ReceivedMessageRequest> onChatReceived() {
        return (socketIOClient, message, ackRequest) -> {
            var user = userRepository.findById(message.getSenderId()).orElse(null);
            var receivedMessageRequest = Message.builder()
                    .id(message.getId())
                    .sender(user)
                    .content(message.getContent())
                    .createdDate(message.getCreatedDate())
                    .type(message.getType())
                    .channel(channelRepository.findById(message.getGroupId()).orElse(null))
                    .isEdited(message.getIsEdited())
                    .editedAt(message.getEditedAt())
                    .status(message.getStatus())
                    .reply(message.getReply())
                    .isForward(message.getIsForward())
                    .build();
            Message newMessage = messageService.saveMessage(receivedMessageRequest);
            notificationService.sendNewMessageNotification(newMessage);

            MessageDetailResponse response = messageService.mappingToMessageDetailResponse(newMessage, message.getSenderId());

            socketIOService.sendMessage(socketIOClient, response, newMessage.getChannel().getGroup().getId());

            var updateLastMessageCommand = UpdateLastMessageCommand.builder()
                    .message(receivedMessageRequest)
                    .channel(receivedMessageRequest.getChannel()).build();
            logger.info("[*] Update last message of channel {}.", receivedMessageRequest.getChannel().getGroup().getId());
            pipeline.send(updateLastMessageCommand);
        };
    }

    private DataListener<DoVotingRequest> onVotingReceived() {
        return (socketIOClient, doVoting, ackRequest) -> {
            Vote updatedVote = voteService.doVoting(doVoting, "dummy");
            VoteDetailResponse response = modelMapper.map(updatedVote, VoteDetailResponse.class);
            socketIOService.sendUpdatedVote(socketIOClient, response);
        };
    }
}