package com.hcmus.mentor.backend.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.RemoveReactionResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.UpdateMessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;

public interface SocketIOService {
    void sendMessage(SocketIOClient client, MessageDetailResponse message, String groupId);

    void sendUpdatedVote(SocketIOClient client, VoteDetailResponse vote);

    void sendBroadcastMessage(MessageDetailResponse message, String groupId);

    void sendReact(ReactMessageResponse request, String groupId);

    void sendRemoveReact(RemoveReactionResponse request, String groupId);

    void sendUpdateMessage(UpdateMessageResponse response, String groupId);

    void sendNewPinMessage(MessageDetailResponse message);

    void sendNewUnpinMessage(String groupId, String messageId);
}
