package com.hcmus.mentor.backend.payload.response.messages;

import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.payload.request.ReactMessageRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReactMessageResponse {

  private String messageId;

  private String emojiId;

  private String senderId;

  private String name;

  private String imageUrl;

  public static ReactMessageResponse from(ReactMessageRequest request, User reactor) {
    String imageUrl = null;
    if (reactor != null
        && !("https://graph.microsoft.com/v1.0/me/photo/$value").equals(reactor.getImageUrl())) {
      imageUrl = reactor.getImageUrl();
    }
    return ReactMessageResponse.builder()
        .emojiId(request.getEmojiId())
        .messageId(request.getMessageId())
        .senderId(request.getSenderId())
        .name((reactor == null) ? null : reactor.getName())
        .imageUrl(imageUrl)
        .build();
  }
}
