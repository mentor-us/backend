package com.hcmus.mentor.backend.payload.response.file;

import lombok.*;
import org.springframework.core.io.InputStreamResource;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownloadFileResponse {
  private InputStreamResource stream;
  private String contentType;
}
