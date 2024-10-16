package com.hcmus.mentor.backend.controller.payload.request.groups;

import lombok.*;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGroupImageRequest {
    @Required
    private String groupId;
    @Required
    private MultipartFile file;
}
