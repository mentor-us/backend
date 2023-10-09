package com.hcmus.mentor.backend.payload.request;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UpdateTaskRequest {
  private String title;
  private String description;
  private Date deadline;
  private List<String> userIds = new ArrayList<>();
  private String parentTask;
}
