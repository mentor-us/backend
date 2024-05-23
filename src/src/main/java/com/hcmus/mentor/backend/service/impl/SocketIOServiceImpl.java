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

    private final SocketIOServer socketServer;
    private static final Logger logger = LoggerFactory.getLogger(SocketIOServiceImpl.class);

    @Override
    public void sendMessage(SocketIOClient client, MessageDetailResponse message, String groupId) {
        logger.info("[*] Send SocketIO - event: receive_message - client: {}", client.getSessionId());
        client.getNamespace()
                .getRoomOperations(groupId)
                .getClients()
                .forEach(receiver -> {
//                    if (!receiver.getSessionId().equals(client.getSessionId())) {
                        logger.info("[*] Send SocketIO - event: receive_message - client: {}", client.getAllRooms());
                        receiver.sendEvent("receive_message", message);
//                    }

                });
    }

    @Override
    public void sendUpdatedVote(SocketIOClient client, VoteDetailResponse vote) {
        logger.info("[*] Send SocketIO - event: receive_voting - client: {}",  client.getNamespace()
                .getRoomOperations(vote.getGroupId())
                .getClients().stream().toList());
        client.getNamespace()
                .getRoomOperations(vote.getGroupId())
                .getClients()
                .forEach(receiver -> {
                    if (receiver.getSessionId().equals(client.getSessionId())) {
                        return;
                    }
                    receiver.sendEvent("receive_voting", vote);
                });
    }

    @Override
    public void sendBroadcastMessage(MessageDetailResponse message, String groupId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        logger.info("[*] Send SocketIO - event: receive_message - clients: {}", clients);
        clients.forEach(client -> client.sendEvent("receive_message", message));
    }

    @Override
    public void sendReact(ReactMessageResponse request, String groupId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        logger.info("[*] Send SocketIO - event: receive_react_message - clients: {}", clients.stream().map(SocketIOClient::getSessionId).toList());
        clients.forEach(client -> client.sendEvent("receive_react_message", request));
    }

    private List<SocketIOClient> getOthers(String groupId, String senderId) {
        var clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
//        logger.debug("[*] Send SocketIO - event: receive_message - clients: {} ", clients);
        return clients.stream()
                .filter(client -> {
                    String userId = client.getHandshakeData().getSingleUrlParam("userId");
                    if (userId == null) {
                        return true;
                    }
                    return !userId.equals(senderId);
                })
                .toList();
    }

    @Override
    public void sendRemoveReact(RemoveReactionResponse request, String groupId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        logger.info("[*] Send SocketIO - event: receive_remove_react_message - clients: {} ", clients.stream().map(SocketIOClient::getSessionId).toList());
        clients.forEach(client -> client.sendEvent("receive_remove_react_message", request));
    }

    @Override
    public void sendUpdateMessage(UpdateMessageResponse response, String groupId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        logger.info("[*] Send SocketIO - event: update_message - clients: {} ", clients.stream().map(SocketIOClient::getSessionId).toList());
        clients.forEach(client -> client.sendEvent("update_message", response));
    }

    @Override
    public void sendNewPinMessage(MessageDetailResponse message) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(message.getGroupId()).getClients());
        logger.info("[*] Send SocketIO - event: receive_pinned_message - clients: {} ", clients.stream().map(SocketIOClient::getSessionId).toList());
        clients.forEach(client -> client.sendEvent("receive_pinned_message", message));
    }

    @Override
    public void sendNewUnpinMessage(String groupId, String messageId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        logger.info("[*] Send SocketIO - event: receive_unpinned_message - clients: {} ", clients.stream().map(SocketIOClient::getSessionId).toList());
        clients.forEach(client -> client.sendEvent("receive_unpinned_message", messageId));
    }
}