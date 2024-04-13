package com.hcmus.mentor.backend.controller.usecase.group.creategroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.request.groups.CreateGroupRequest;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command for creating a group.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupCommand implements Command<GroupServiceDto> {
    private String creatorEmail;
    private CreateGroupRequest request;
}
