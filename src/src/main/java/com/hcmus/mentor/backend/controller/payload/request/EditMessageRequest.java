package com.hcmus.mentor.backend.controller.payload.request;

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
