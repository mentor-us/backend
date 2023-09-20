package com.hcmus.mentor.backend.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendImagesRequest {

    private String id;

    @NotNull
    private String groupId;

    @NotNull
    private String senderId;

    private MultipartFile[] files;
}
