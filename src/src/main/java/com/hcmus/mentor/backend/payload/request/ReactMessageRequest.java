package com.hcmus.mentor.backend.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReactMessageRequest {

    @NotNull
    private String messageId;

    @NotNull
    private String emojiId;

    @NotNull
    private String senderId;
}
