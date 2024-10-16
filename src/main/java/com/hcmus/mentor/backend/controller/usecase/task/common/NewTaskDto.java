package com.hcmus.mentor.backend.controller.usecase.task.common;

import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewTaskDto {

    private String id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private String parentTask;
    private Channel group;
    private User assigner;
    private TaskStatus status;
    private LocalDateTime createdDate;
}
