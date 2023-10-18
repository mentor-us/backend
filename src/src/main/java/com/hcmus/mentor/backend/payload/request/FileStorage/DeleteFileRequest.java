package com.hcmus.mentor.backend.payload.request.FileStorage;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteFileRequest {

  @Parameter(required = true)
  private String key;
}
