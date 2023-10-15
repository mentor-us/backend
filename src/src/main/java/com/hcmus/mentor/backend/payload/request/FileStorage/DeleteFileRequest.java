package com.hcmus.mentor.backend.payload.request.FileStorage;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteFileRequest {
  private String key;
}
