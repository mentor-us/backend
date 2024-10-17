package vn.edu.hcmus.mentor.controller.payload.response.meetings;

import vn.edu.hcmus.mentor.domain.constant.MeetingRepeated;
import vn.edu.hcmus.mentor.service.dto.GroupDto;
import vn.edu.hcmus.mentor.service.dto.UserDto;
import lombok.*;

import java.util.Date;
import java.util.List;

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

    private MeetingRepeated repeated;

    private String place;

    private UserDto organizer;

    private GroupDto group;

    @Builder.Default
    private String type = "MEETING";

    private boolean isAll;

    private boolean canEdit;

    private int totalAttendees;

    private List<MeetingHistoryDetail> histories;
}