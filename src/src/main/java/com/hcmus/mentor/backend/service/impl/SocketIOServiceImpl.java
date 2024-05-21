package com.hcmus.mentor.backend.service.impl;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.RemoveReactionResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.UpdateMessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.service.SocketIOService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SocketIOServiceImpl implements SocketIOService {

    private static final Logger logger = LoggerFactory.getLogger(SocketIOServiceImpl.class);

    private final SocketIOServer socketServer;

    @Override
    public void sendMessage(SocketIOClient client, MessageDetailResponse message, String groupId) {
        client.getNamespace()
                .getRoomOperations(groupId)
                .getClients()
                .forEach(receiver -> {
                    if (receiver.getSessionId().equals(client.getSessionId())) {
                        return;
                    }

                    receiver.sendEvent("receive_message", message);
                });

        logger.info("sendMessage: {}, client {}, message {}", "receive_message", client, message);
    }

    @Override
    public void sendUpdatedVote(SocketIOClient client, VoteDetailResponse vote) {
        client.getNamespace()
                .getRoomOperations(vote.getGroupId())
                .getClients()
                .forEach(receiver -> {
                    if (receiver.getSessionId().equals(client.getSessionId())) {
                        return;
                    }
                    receiver.sendEvent("receive_voting", vote);
                });

        logger.info("sendUpdatedVote: {}, client {}, message {}", "receive_voting", client, vote);
    }

    @Override
    public void sendBroadcastMessage(MessageDetailResponse message, String groupId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        clients.forEach(client -> client.sendEvent("receive_message", message));

        logger.info("sendBroadcastMessage: {}, client {}, message {}", "receive_message", clients, message);
    }

    @Override
    public void sendReact(ReactMessageResponse request, String groupId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        clients.forEach(client -> client.sendEvent("receive_react_message", request));

        logger.info("sendReact: {}, client {}, message {}", "receive_react_message", clients, request);
    }

    private List<SocketIOClient> getOthers(String groupId, String senderId) {
        var abc = socketServer.getRoomOperations(groupId).getClients().stream()
                .filter(client -> {
                    String userId = client.getHandshakeData().getSingleUrlParam("userId");
                    if (userId == null) {
                        return true;
                    }
                    return !userId.equals(senderId);
                })
                .toList();

        logger.info("getOthers: {}, client {}, message {}", "receive_message", socketServer.getRoomOperations(groupId).getClients(), groupId);

        return abc;
    }

    @Override
    public void sendRemoveReact(RemoveReactionResponse request, String groupId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        clients.forEach(client -> client.sendEvent("receive_remove_react_message", request));

        logger.info("sendRemoveReact: {}, client {}, message {}", "receive_message", clients, groupId);
    }

    @Override
    public void sendUpdateMessage(UpdateMessageResponse response, String groupId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        clients.forEach(client -> client.sendEvent("update_message", response));

        logger.info("sendUpdateMessage: {}, client {}, message {}", "update_message", clients, response);
    }

    @Override
    public void sendNewPinMessage(MessageDetailResponse message) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(message.getGroupId()).getClients());
        clients.forEach(client -> client.sendEvent("receive_pinned_message", message));

        logger.info("sendNewPinMessage: {}, client {}, message {}", "receive_pinned_message", clients, message);
    }

    @Override
    public void sendNewUnpinMessage(String groupId, String messageId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        clients.forEach(client -> client.sendEvent("receive_unpinned_message", messageId));

        logger.info("sendNewUnpinMessage: {}, client {}, message {}", "receive_unpinned_message", clients, messageId);
    }
}
