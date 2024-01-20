package com.hcmus.mentor.backend.controller.payload.response.tasks;

import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.User;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse {
    private String id;
    private String title;
    private String description;
    private Date deadline;
    private String parentTask;
    private Group group;
    private User assigner;
    private Task.Status status;
    private Date createdDate;

    public static TaskResponse from(Task task, User assigner, Task.Assignee assignee, Group group) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .parentTask(task.getParentTask())
                .group(group)
                .assigner(assigner)
                .status(assignee == null ? null : assignee.getStatus())
                .createdDate(task.getCreatedDate())
                .build();
    }
}
