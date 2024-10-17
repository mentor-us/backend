package vn.edu.hcmus.mentor.backend.controller.payload.response.tasks;

import vn.edu.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import vn.edu.hcmus.mentor.backend.domain.Task;
import vn.edu.hcmus.mentor.backend.domain.User;
import vn.edu.hcmus.mentor.backend.domain.constant.TaskStatus;
import vn.edu.hcmus.mentor.backend.domain.dto.AssigneeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private String id;
    private String title;
    private String description;
    private Date deadline;
    private String parentTask;
    private GroupDetailDto group;
    private User assigner;
    private TaskStatus status;
    private Date createdDate;

    public static TaskResponse from(Task task, AssigneeDto assignee, GroupDetailDto group) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .parentTask(task.getParentTask() == null ? null : task.getParentTask().getId())
                .group(group)
                .assigner(task.getAssigner())
                .status(assignee == null ? null : assignee.getStatus())
                .createdDate(task.getCreatedDate())
                .build();
    }
}
