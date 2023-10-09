package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UpdateStatusByMentorRequest {
  private String emailUserAssigned;
  private Task.Status status;
}
