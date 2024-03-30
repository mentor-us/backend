package com.hcmus.mentor.backend.controller.payload.response.tasks;

import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskMessageResponse {

    private String id;

    private String title;

    private String description;

    private Date deadline;

    private String assignerId;

    @Builder.Default
    private List<TaskAssigneeResponse> assignees = new ArrayList<>();

    private String groupId;

    private Date createdDate;

    private TaskStatus status;

    public static TaskMessageResponse from(Task task) {
        return TaskMessageResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .createdDate(task.getCreatedDate())
                .assignerId(task.getAssignerId())
                .groupId(task.getGroupId())
                .build();
    }
}
