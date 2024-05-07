package com.hcmus.mentor.backend.controller.payload.response.tasks;

import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TaskDetailResponse {

    private String id;

    private String title;

    private String description;

    private Date deadline;

    private TaskDetailResponseAssigner assigner;

    private int totalAssignees;

    private String parentTask;

    private TaskDetailResponseGroup group;

    private Date createdDate;

    private TaskDetailResponseRole role;

    @Builder.Default
    private TaskStatus status = null;

    public static TaskDetailResponse from(Task task) {
        return TaskDetailResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .createdDate(task.getCreatedDate())
                .totalAssignees(task.getAssignees().size())
                .build();
    }

    public static TaskDetailResponse from(
            Task task,
            TaskDetailResponseAssigner assigner,
            TaskDetailResponseGroup group,
            TaskDetailResponseRole role,
            TaskStatus status) {
        TaskDetailResponse response = from(task);
        response.setAssigner(assigner);
        response.setGroup(group);
        response.setRole(role);
        response.setStatus(status);
        return response;
    }
}
