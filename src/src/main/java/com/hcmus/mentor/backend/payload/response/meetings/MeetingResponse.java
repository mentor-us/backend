package com.hcmus.mentor.backend.payload.response.meetings;

import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.Meeting;
import com.hcmus.mentor.backend.entity.User;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingResponse implements Serializable {

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

  private List<Meeting.MeetingHistory> histories;

  public static MeetingResponse from(Meeting meeting, User organizer, Group group) {
    return MeetingResponse.builder()
        .id(meeting.getId())
        .title(meeting.getTitle())
        .description(meeting.getDescription())
        .timeStart(meeting.getTimeStart())
        .timeEnd(meeting.getTimeEnd())
        .repeated(meeting.getRepeated())
        .place(meeting.getPlace())
        .organizer(organizer)
        .group(group)
        .histories(meeting.getHistories())
        .build();
  }

  @Override
  public String toString() {
    return "Cuộc hẹn:"
        + "id='"
        + id
        + '\''
        + ", Tiêu đề='"
        + title
        + '\''
        + ", Mô tả='"
        + description
        + '\''
        + ", Thời gian bắt đầu="
        + timeStart
        + ", Thời gian kết thúc="
        + timeEnd
        + ", Địa điểm ='"
        + place
        + '\''
        + ", Người tạo="
        + organizer.toString()
        + ", Lịch sử cuộc hẹn="
        + histories;
  }
}
