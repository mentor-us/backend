package com.hcmus.mentor.backend.controller.payload.response.meetings;

import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.MeetingHistory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingHistoryDetail {

    private String id;

    private LocalDateTime timeStart;

    private LocalDateTime timeEnd;

    private String place;

    private ShortProfile modifier;

    private LocalDateTime modifyDate;

    public static MeetingHistoryDetail from(MeetingHistory history, ShortProfile modifier) {
        return MeetingHistoryDetail.builder()
                .id(history.getId())
                .timeStart(history.getTimeStart())
                .timeEnd(history.getTimeEnd())
                .place(history.getPlace())
                .modifier(modifier)
                .modifyDate(history.getModifyDate())
                .build();
    }
}