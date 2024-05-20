package com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupWorkspaceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query to get group detail
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupWorkSpaceQuery implements Command<GroupWorkspaceDto> {

    private String groupId;
}
