package com.hcmus.mentor.backend.payload.response.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

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
