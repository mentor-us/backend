package vn.edu.hcmus.mentor.backend.controller.socketio;

import an.awesome.pipelinr.Pipeline;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import vn.edu.hcmus.mentor.backend.controller.payload.request.messages.JoinOutRoomRequest;
import vn.edu.hcmus.mentor.backend.controller.payload.request.messages.ReceivedMessageRequest;
import vn.edu.hcmus.mentor.backend.controller.payload.request.votes.DoVotingRequest;
import vn.edu.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import vn.edu.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import vn.edu.hcmus.mentor.backend.controller.usecase.channel.updatelastmessage.UpdateLastMessageCommand;
import vn.edu.hcmus.mentor.backend.domain.Message;
import vn.edu.hcmus.mentor.backend.domain.Vote;
import vn.edu.hcmus.mentor.backend.repository.ChannelRepository;
import vn.edu.hcmus.mentor.backend.repository.UserRepository;
import vn.edu.hcmus.mentor.backend.service.MessageService;
import vn.edu.hcmus.mentor.backend.service.NotificationService;
import vn.edu.hcmus.mentor.backend.service.SocketIOService;
import vn.edu.hcmus.mentor.backend.service.VoteService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SocketController {

    private static final Logger logger = LoggerFactory.getLogger(SocketController.class);
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

        logger.debug("[*] Configure Socket IO Server listener.");
    }

    private DataListener<JoinOutRoomRequest> onJoinRoom() {
        return (client, payload, ackRequest) -> {
            if (payload.getGroupId() == null || payload.getGroupId().isEmpty()) {
                return;
            }
            logger.info("[*] Receive SocketIO - event: join_room - client: {}, payload: {},", client, payload.getGroupId());
            client.joinRoom(payload.getGroupId());
        };
    }

    private DataListener<JoinOutRoomRequest> onOutRoom() {
        return (client, payload, ackRequest) -> {
            if (payload.getGroupId() == null || payload.getGroupId().isEmpty()) {
                return;
            }
            logger.debug("[*] Receive SocketIO - event: out_room - client: {}, payload: {},", client, payload.getGroupId());
            client.leaveRoom(payload.getGroupId());
        };
    }

    private DataListener<ReceivedMessageRequest> onChatReceived() {
        return (socketIOClient, message, ackRequest) -> {
            logger.info("[*] Receive SocketIO - event: send_message - response: {}", message.getGroupId());
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
            notificationService.sendForMessage(newMessage);

            MessageDetailResponse response = messageService.mappingToMessageDetailResponse(newMessage, message.getSenderId());

            socketIOService.sendMessage(socketIOClient, response, newMessage.getChannel().getId());

            var updateLastMessageCommand = UpdateLastMessageCommand.builder()
                    .message(receivedMessageRequest)
                    .channel(receivedMessageRequest.getChannel()).build();
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