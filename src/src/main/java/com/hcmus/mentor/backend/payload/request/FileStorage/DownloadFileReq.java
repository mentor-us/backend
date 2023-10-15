package com.hcmus.mentor.backend.payload.request.FileStorage;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownloadFileReq {
  private String key;
}
