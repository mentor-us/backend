package com.hcmus.mentor.backend.payload.response.tasks;

import com.hcmus.mentor.backend.entity.Task;
import com.hcmus.mentor.backend.entity.User;
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

    private String parentTask = "";

    private Group group;

    private Date createdDate;

    private Role role;

    @Builder.Default
    private Task.Status status = null;

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

        public static Group from(com.hcmus.mentor.backend.entity.Group group) {
            return TaskDetailResponse.Group.builder()
                    .id(group.getId())
                    .name(group.getName())
                    .build();
        }
    }

    public enum Role {
        MENTOR,
        MENTEE,
    }

    public static TaskDetailResponse from(Task task) {
        return TaskDetailResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .createdDate(task.getCreatedDate())
                .totalAssignees(task.getAssigneeIds().size())
                .build();
    }

    public static TaskDetailResponse from(Task task,
                                          TaskDetailResponse.Assigner assigner,
                                          Group group,
                                          TaskDetailResponse.Role role,
                                          Task.Status status) {
        TaskDetailResponse response = from(task);
        response.setAssigner(assigner);
        response.setGroup(group);
        response.setRole(role);
        response.setStatus(status);
        return response;
    }
}
