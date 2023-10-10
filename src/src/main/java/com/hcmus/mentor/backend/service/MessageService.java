package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.entity.*;
import com.hcmus.mentor.backend.payload.request.ReactMessageRequest;
import com.hcmus.mentor.backend.payload.request.SendFileRequest;
import com.hcmus.mentor.backend.payload.request.SendImagesRequest;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface MessageService {
  List<MessageDetailResponse> getGroupMessages(String viewerId, String groupId, int page, int size);

  Reaction fulfillReaction(Reaction reaction, User reactor);

  MessageDetailResponse fulfillTextMessage(MessageResponse message);

  MessageDetailResponse fulfillMeetingMessage(MessageResponse message);

  MessageDetailResponse fulfillTaskMessage(MessageResponse message);

  List<MessageResponse> findGroupMessagesByText(String groupId, String query, int page, int size);

  Message saveMessage(Message data);

  void reactMessage(ReactMessageRequest request);

  void removeReaction(String messageId, String senderId);

  MessageDetailResponse.TotalReaction calculateTotalReactionMessage(Message message);

  Message saveImageMessage(SendImagesRequest request) throws GeneralSecurityException, IOException;

  Message saveFileMessage(SendFileRequest request) throws GeneralSecurityException, IOException;

  Message saveTaskMessage(Task task);

  Message saveVoteMessage(Vote vote);
}
