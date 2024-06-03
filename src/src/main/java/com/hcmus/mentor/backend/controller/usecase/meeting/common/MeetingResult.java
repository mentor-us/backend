package com.hcmus.mentor.backend.controller.usecase.meeting.common;

import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.controller.usecase.channel.common.ChannelDetailDto;
import com.hcmus.mentor.backend.domain.MeetingHistory;
import com.hcmus.mentor.backend.domain.constant.MeetingRepeated;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingResult {

    private String id;

    private String title;

    private String description;

    private Date timeStart;

    private Date timeEnd;

    private MeetingRepeated repeated;

    private String place;

    private ShortProfile organizer;

    private ChannelDetailDto channel;

    private Date createdDate;

    @Builder.Default
    private String type = "MEETING";

    private List<MeetingHistory> histories;
}