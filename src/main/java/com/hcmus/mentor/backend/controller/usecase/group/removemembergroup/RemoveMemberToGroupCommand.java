package com.hcmus.mentor.backend.controller.usecase.group.removemembergroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveMemberToGroupCommand implements Command<GroupDetailDto> {

    private String groupId;
    private String userId;
}
