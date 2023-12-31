package com.hcmus.mentor.backend.payload.response.analytic;

import com.hcmus.mentor.backend.entity.Group;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GroupAnalyticResponse {
  private String id;
  private String name;
  private String category;
  private Date lastTimeActive;
  private long totalMessages;
  private long totalTasks;
  private long totalMeetings;
  private long totalMentees;
  private long totalMentors;
  private Group.Status status;

  private List<Member> members;

  @Getter
  @Setter
  @AllArgsConstructor
  @Builder
  public static class Member {
    private String id;
    private String email;
    private String name;
    private int trainingPoint;
    private Boolean hasEnglishCert;
    private Double studyingPoint;
    private String role;
    private long totalMessages;
    private long totalMeetings;
    private long totalTasks;
    private long totalDoneTasks;
    private Date lastTimeActive;
  }
}
