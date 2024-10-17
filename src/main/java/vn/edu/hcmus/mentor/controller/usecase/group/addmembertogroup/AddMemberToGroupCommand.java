package vn.edu.hcmus.mentor.controller.usecase.group.addmembertogroup;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.usecase.group.common.GroupDetailDto;
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
