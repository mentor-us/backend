package com.hcmus.mentor.backend.controller.usecase.group.updategroupbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.group.searchgroup.GroupDetailDto;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Command to find own groups.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGroupByIdCommand implements Command<GroupDetailDto> {

    private String id;
    private String name;
    private String description;
    private GroupStatus status;
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;
    private String groupCategory;
}
