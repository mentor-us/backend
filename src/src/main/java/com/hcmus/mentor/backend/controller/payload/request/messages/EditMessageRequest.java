package com.hcmus.mentor.backend.controller.payload.request.messages;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class EditMessageRequest {

    private String messageId;

    private String newContent;
}