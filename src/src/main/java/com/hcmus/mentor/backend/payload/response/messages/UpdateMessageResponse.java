package com.hcmus.mentor.backend.payload.response.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateMessageResponse {

    private String messageId;

    private String newContent;

    private Action action;

    public enum Action {
        update,
        delete
    }
}
