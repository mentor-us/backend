package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;

import java.util.List;

public interface MessageService2 {
    List<MessageDetailResponse> fulfillMessages(List<MessageResponse> messages, String viewerId);

    String getLastGroupMessage(String groupId);
}
