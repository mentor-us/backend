package com.hcmus.mentor.backend.service.dto;

import com.hcmus.mentor.backend.domain.constant.MeetingRepeated;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingDto {

    private String id;

    private String title;

    private String description;

    private Date timeStart;

    private Date timeEnd;

    private MeetingRepeated repeated;

    private String place;

    private String organizerId;

    @Builder.Default
    private List<String> attendees = new ArrayList<>();

    private String groupId;

    private Date createdDate;

    @Builder.Default
    private List<MeetingHistoryDto> histories = new ArrayList<>();

    public static MeetingDto from(com.hcmus.mentor.backend.domain.Meeting meeting) {
        return MeetingDto.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .timeStart(meeting.getTimeStart())
                .timeEnd(meeting.getTimeEnd())
                .repeated(meeting.getRepeated())
                .place(meeting.getPlace())
                .organizerId(meeting.getOrganizer() != null ? meeting.getOrganizer().getId() : null)
                .attendees(meeting.getAttendees().stream().map(com.hcmus.mentor.backend.domain.User::getId).toList())
                .groupId(meeting.getGroup().getId())
                .createdDate(meeting.getCreatedDate())
                .histories(meeting.getHistories().stream().map(MeetingHistoryDto::from).toList())
                .build();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MeetingHistoryDto {

        private String id;

        private Date timeStart;

        private Date timeEnd;

        private String place;

        private String modifierId;

        private Date modifyDate;

        public static MeetingHistoryDto from(com.hcmus.mentor.backend.domain.MeetingHistory history) {
            return MeetingHistoryDto.builder()
                    .id(history.getId())
                    .timeStart(history.getTimeStart())
                    .timeEnd(history.getTimeEnd())
                    .place(history.getPlace())
                    .modifierId(history.getModifier().getId())
                    .modifyDate(history.getModifyDate())
                    .build();
        }

        @Override
        public String toString() {
            return "\tLịch hẹn: "
                    + "id='"
                    + id
                    + '\''
                    + ", Thời gian bắt đầu="
                    + timeStart
                    + ", Thời gian kết thúc="
                    + timeEnd
                    + ", Địa điểm='"
                    + place
                    + '\''
                    + ", Ngày cập nhật ="
                    + modifyDate
                    + '\n';
        }
    }
}