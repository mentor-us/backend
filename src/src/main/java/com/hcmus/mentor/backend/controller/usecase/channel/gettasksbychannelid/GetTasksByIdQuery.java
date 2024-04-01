package com.hcmus.mentor.backend.controller.usecase.channel.gettasksbychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskDetailResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class GetTasksByIdQuery implements Command<List<TaskDetailResponse>> {

    /**
     * The ID of the channel to retrieve.
     */
    private String id;
}
