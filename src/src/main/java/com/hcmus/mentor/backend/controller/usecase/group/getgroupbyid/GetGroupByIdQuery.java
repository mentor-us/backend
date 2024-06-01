package com.hcmus.mentor.backend.controller.usecase.group.getgroupbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to find own groups.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetGroupByIdQuery implements Command<GroupDetailDto> {

    private String id;

    private boolean isDetail = false;
}
