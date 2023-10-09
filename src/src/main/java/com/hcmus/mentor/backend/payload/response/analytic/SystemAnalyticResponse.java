package com.hcmus.mentor.backend.payload.response.analytic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SystemAnalyticResponse {
  private long totalGroups;
  private long activeGroups;
  private long totalTasks;
  private long totalMessages;
  private long totalMeetings;
  private long totalUsers;
  private long activeUsers;
}
