package com.hcmus.mentor.backend.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.payload.response.messages.RemoveReactionResponse;
import com.hcmus.mentor.backend.payload.response.messages.UpdateMessageResponse;
import com.hcmus.mentor.backend.payload.response.votes.VoteDetailResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SocketIOService {

    private final SocketIOServer socketServer;

    public SocketIOService(SocketIOServer socketServer) {
        this.socketServer = socketServer;
    }

    public void sendMessage(SocketIOClient client, MessageDetailResponse message, String groupId) {
        client.getNamespace().getRoomOperations(groupId).getClients().forEach(receiver -> {
            if (receiver.getSessionId().equals(client.getSessionId())) {
                return;
            }
            receiver.sendEvent("receive_message", message);
        });
    }

    public void sendUpdatedVote(SocketIOClient client, VoteDetailResponse vote) {
        client.getNamespace().getRoomOperations(vote.getGroupId())
                .getClients().forEach(receiver -> {
                    if (receiver.getSessionId().equals(client.getSessionId())) {
                        return;
                    }
                    receiver.sendEvent("receive_voting", vote);
                });
    }

    public void sendBroadcastMessage(MessageDetailResponse message, String groupId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        clients.forEach(client -> client.sendEvent("receive_message", message));
    }

    public void sendReact(ReactMessageResponse request, String groupId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        clients.forEach(client -> client.sendEvent("receive_react_message", request));
    }

    private List<SocketIOClient> getOthers(String groupId, String senderId) {
        return socketServer.getRoomOperations(groupId).getClients()
                .stream()
                .filter(client -> {
                    String userId = client.getHandshakeData()
                            .getSingleUrlParam("userId");
                    if (userId == null) {
                        return true;
                    }
                    return !userId.equals(senderId);
                })
                .collect(Collectors.toList());
    }

    public void sendRemoveReact(RemoveReactionResponse request, String groupId) {
        List<SocketIOClient> clients = new ArrayList<>(socketServer.getRoomOperations(groupId).getClients());
        clients.forEach(client -> client.sendEvent("receive_remove_react_message", request));
    }

    public void sendUpdateMessage(UpdateMessageResponse response, String groupId) {
        List<SocketIOClient> clients = new ArrayList<>(
                socketServer.getRoomOperations(groupId).getClients());
        clients.forEach(client -> client.sendEvent("update_message", response));
    }

    public void sendNewPinMessage(MessageDetailResponse message) {
        List<SocketIOClient> clients = new ArrayList<>(
                socketServer.getRoomOperations(message.getGroupId()).getClients());
        clients.forEach(client -> client.sendEvent("receive_pinned_message", message));
    }

    public void sendNewUnpinMessage(String groupId, String messageId) {
        List<SocketIOClient> clients = new ArrayList<>(
                socketServer.getRoomOperations(groupId).getClients());
        clients.forEach(client -> client.sendEvent("receive_unpinned_message", messageId));
    }
}
