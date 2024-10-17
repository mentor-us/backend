package vn.edu.hcmus.mentor.controller.usecase.group.enabledisablestatusgroupbyid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.usecase.group.common.GroupDetailDto;
import vn.edu.hcmus.mentor.domain.constant.GroupStatus;
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
