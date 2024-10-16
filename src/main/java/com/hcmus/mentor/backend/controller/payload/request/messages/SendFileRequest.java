package com.hcmus.mentor.backend.controller.payload.request.messages;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendFileRequest {

    private String id;

    @NotNull
    private String groupId;

    @NotNull
    private String senderId;

    private MultipartFile file;
}