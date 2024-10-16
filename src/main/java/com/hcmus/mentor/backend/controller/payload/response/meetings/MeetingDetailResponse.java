package com.hcmus.mentor.backend.controller.payload.response.meetings;

import com.hcmus.mentor.backend.domain.constant.MeetingRepeated;
import com.hcmus.mentor.backend.service.dto.GroupDto;
import com.hcmus.mentor.backend.service.dto.UserDto;
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