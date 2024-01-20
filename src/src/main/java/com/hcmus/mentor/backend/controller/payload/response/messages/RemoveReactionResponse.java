package com.hcmus.mentor.backend.controller.payload.response.messages;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RemoveReactionResponse {

    @NotNull
    private String messageId;

    @NotNull
    private String senderId;

    private MessageDetailResponse.TotalReaction totalReaction;
}
