package com.hcmus.mentor.backend.controller.payload.response.tasks;

import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.domain.Assignee;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TaskAssigneeResponse {

    private String id;

    private String name;

    private String email;

    private String imageUrl;

    private TaskStatus status;

    private boolean isMentor;

    public TaskAssigneeResponse(Assignee assignee, Boolean isMentor) {
        this.id = assignee.getUser().getId();
        this.name = assignee.getUser().getName();
        this.email = assignee.getUser().getEmail();
        this.imageUrl = assignee.getUser().getImageUrl();
        this.status = assignee.getStatus();
        this.isMentor = isMentor;
    }

    public static TaskAssigneeResponse from(
            ProfileResponse profile, TaskStatus status, boolean isMentor) {
        String imageUrl = profile.getImageUrl();
        if (("https://graph.microsoft.com/v1.0/me/photo/$value").equals(imageUrl)) {
            imageUrl = null;
        }
        return TaskAssigneeResponse.builder()
                .id(profile.getId())
                .name(profile.getName())
                .email(profile.getEmail())
                .imageUrl(imageUrl)
                .status(status)
                .isMentor(isMentor)
                .build();
    }
}
