package com.hcmus.mentor.backend.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendImagesRequest {

  private String id;

  @NotNull private String groupId;

  @NotNull private String senderId;

  private MultipartFile[] files;
}
