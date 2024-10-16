package com.hcmus.mentor.backend.controller.usecase.group.importgroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Command to import group
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportGroupCommand implements Command<GroupServiceDto> {

    MultipartFile file;
}
