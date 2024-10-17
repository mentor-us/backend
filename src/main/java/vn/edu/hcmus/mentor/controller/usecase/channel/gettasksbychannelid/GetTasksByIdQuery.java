package vn.edu.hcmus.mentor.controller.usecase.channel.gettasksbychannelid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.usecase.task.common.TaskDetailResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class GetTasksByIdQuery implements Command<List<TaskDetailResult>> {

    /**
     * The ID of the channel to retrieve.
     */
    private String id;
}
