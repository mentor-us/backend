package com.hcmus.mentor.backend.controller.usecase.group.promotedemoteuser;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromoteDenoteUserByIdCommand implements Command<GroupDetailDto> {

    private String userId;
    private String groupId;
    private Boolean toMentor;
}
