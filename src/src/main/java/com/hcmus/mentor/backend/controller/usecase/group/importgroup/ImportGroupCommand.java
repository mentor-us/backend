package com.hcmus.mentor.backend.controller.usecase.group.importgroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Command to import group
 */
@Data
@AllArgsConstructor
public class ImportGroupCommand implements Command<GroupServiceDto> {
    String emailUser;

    MultipartFile file;
}
