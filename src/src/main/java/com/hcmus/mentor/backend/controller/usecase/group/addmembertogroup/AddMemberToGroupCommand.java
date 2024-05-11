package com.hcmus.mentor.backend.controller.usecase.group.addmembertogroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberToGroupCommand implements Command<GroupDetailDto> {

    private String groupId;
    private List<String> emails;
    private boolean isMentor;
}
