package com.hcmus.mentor.backend.controller.payload.request.FileStorage;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownloadFileReq {

    @Parameter(required = true)
    private String key;
}
