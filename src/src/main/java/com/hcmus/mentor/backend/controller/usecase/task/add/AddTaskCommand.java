package com.hcmus.mentor.backend.controller.usecase.task.add;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTaskCommand implements Command<Task> {

    private String title;
    private String description;
    private Date deadline;
    private List<String> userIds = new ArrayList<>();
    private String parentTask;
    private String groupId;
}
