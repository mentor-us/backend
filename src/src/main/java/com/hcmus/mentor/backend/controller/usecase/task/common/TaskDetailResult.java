package com.hcmus.mentor.backend.controller.usecase.task.common;

import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskDetailResponseAssigner;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskDetailResponseRole;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TaskDetailResult {

    private String id;

    private String title;

    private String description;

    private Date deadline;

    private TaskDetailResponseAssigner assigner;

    private int totalAssignees;

    private String parentTask;

    private TaskDetailResultChannel channel;

    private Date createdDate;

    private TaskDetailResponseRole role;

    @Builder.Default
    private TaskStatus status = null;

    public static TaskDetailResult from(Task task) {
        return TaskDetailResult.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .createdDate(task.getCreatedDate())
                .totalAssignees(task.getAssignees().size())
                .build();
    }

    public static TaskDetailResult from(
            Task task,
            TaskDetailResponseAssigner assigner,
            TaskDetailResultChannel channel,
            TaskDetailResponseRole role,
            TaskStatus status) {
        TaskDetailResult response = from(task);
        response.setAssigner(assigner);
        response.setChannel(channel);
        response.setRole(role);
        response.setStatus(status);
        return response;
    }
}