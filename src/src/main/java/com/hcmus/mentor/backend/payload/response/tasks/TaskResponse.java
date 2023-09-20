package com.hcmus.mentor.backend.payload.response.tasks;

import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.Task;
import com.hcmus.mentor.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

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
