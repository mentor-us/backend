package com.hcmus.mentor.backend.controller.payload.response.meetings;

import com.hcmus.mentor.backend.domain.Meeting;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingHistoryDetail {

    private String id;

    private Date timeStart;

    private Date timeEnd;

    private String place;

    private ShortProfile modifier;

    private Date modifyDate;

    public static MeetingHistoryDetail from(Meeting.MeetingHistory history, ShortProfile modifier) {
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
