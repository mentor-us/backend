package vn.edu.hcmus.mentor.controller.usecase.task.common;

import vn.edu.hcmus.mentor.domain.Channel;
import vn.edu.hcmus.mentor.domain.User;
import vn.edu.hcmus.mentor.domain.constant.TaskStatus;
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
