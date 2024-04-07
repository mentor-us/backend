package com.hcmus.mentor.backend.controller.usecase.channel.gettasksbychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.task.common.TaskDetailResult;
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
