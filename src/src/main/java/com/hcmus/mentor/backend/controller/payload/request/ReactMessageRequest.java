package com.hcmus.mentor.backend.controller.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
