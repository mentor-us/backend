package vn.edu.hcmus.mentor.backend.controller.usecase.group.enabledisablestatusgroupbyid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import vn.edu.hcmus.mentor.backend.domain.constant.GroupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnableDisableGroupByIdCommand implements Command<GroupDetailDto> {

    private String id;
    private GroupStatus status;
}
