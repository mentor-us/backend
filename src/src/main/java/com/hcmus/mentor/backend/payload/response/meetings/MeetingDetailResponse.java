package com.hcmus.mentor.backend.payload.response.meetings;

import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.Meeting;
import com.hcmus.mentor.backend.entity.User;
import java.util.Date;
import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingDetailResponse {
  private String id;

  private String title;

  private String description;

  private Date timeStart;

  private Date timeEnd;

  private Meeting.Repeated repeated;

  private String place;

  private User organizer;

  private Group group;

  @Builder.Default private String type = "MEETING";

  private boolean isAll;

  private boolean canEdit;

  private int totalAttendees;

  private List<MeetingHistoryDetail> histories;
}
