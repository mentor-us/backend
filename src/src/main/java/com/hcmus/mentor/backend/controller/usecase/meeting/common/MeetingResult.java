package com.hcmus.mentor.backend.controller.usecase.meeting.common;

import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.MeetingHistory;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.MeetingRepeated;
import lombok.*;

import java.time.LocalDateTime;
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

    private LocalDateTime timeStart;

    private LocalDateTime timeEnd;

    private MeetingRepeated repeated;

    private String place;

    private User organizer;

    private Channel channel;

    @Builder.Default
    private String type = "MEETING";

    private List<MeetingHistory> histories;
}