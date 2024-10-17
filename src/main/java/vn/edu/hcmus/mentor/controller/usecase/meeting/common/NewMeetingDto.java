package vn.edu.hcmus.mentor.controller.usecase.meeting.common;

import vn.edu.hcmus.mentor.domain.Channel;
import vn.edu.hcmus.mentor.domain.MeetingHistory;
import vn.edu.hcmus.mentor.domain.User;
import vn.edu.hcmus.mentor.domain.constant.MeetingRepeated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewMeetingDto {

    private String id;
    private String title;
    private String description;
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;
    private MeetingRepeated repeated;
    private String place;
    private User organizer;
    private List<String> attendees = new ArrayList<>();
    private Channel group;
    private String type = "MEETING";
    private List<MeetingHistory> histories;
}
