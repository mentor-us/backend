package com.hcmus.mentor.backend.payload.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

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
