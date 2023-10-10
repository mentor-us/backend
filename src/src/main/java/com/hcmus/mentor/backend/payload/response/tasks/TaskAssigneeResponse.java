package com.hcmus.mentor.backend.payload.response.tasks;

import com.hcmus.mentor.backend.entity.Task;
import com.hcmus.mentor.backend.payload.response.users.ProfileResponse;
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

  private Task.Status status;

  private boolean isMentor;

  public static TaskAssigneeResponse from(
      ProfileResponse profile, Task.Status status, boolean isMentor) {
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
