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

    private Assigner assigner;

    private int totalAssignees;

    @Builder.Default
    private String parentTask = "";

    private Group group;

    private Date createdDate;

    private Role role;

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
            TaskDetailResponse.Assigner assigner,
            Group group,
            TaskDetailResponse.Role role,
            TaskStatus status) {
        TaskDetailResponse response = from(task);
        response.setAssigner(assigner);
        response.setGroup(group);
        response.setRole(role);
        response.setStatus(status);
        return response;
    }

    public enum Role {
        MENTOR,
        MENTEE,
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Assigner {
        private String id;
        private String name;
        private String imageUrl;

        public static Assigner from(User user) {
            return TaskDetailResponse.Assigner.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .imageUrl(user.getImageUrl())
                    .build();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Group {
        private String id;
        private String name;

        public static Group from(com.hcmus.mentor.backend.domain.Group group) {
            return TaskDetailResponse.Group.builder().id(group.getId()).name(group.getName()).build();
        }
    }
}
