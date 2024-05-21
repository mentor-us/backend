package com.hcmus.mentor.backend.service.dto;

import com.hcmus.mentor.backend.domain.constant.MeetingRepeated;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private LocalDateTime timeStart;

    private LocalDateTime timeEnd;

    private MeetingRepeated repeated;

    private String place;

    private String organizerId;

    @Builder.Default
    private List<String> attendees = new ArrayList<>();

    private String groupId;

    private LocalDateTime createdDate;

    @Builder.Default
    private List<MeetingHistoryDto> histories = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MeetingHistoryDto {

        private String id;

        private LocalDateTime timeStart;

        private LocalDateTime timeEnd;

        private String place;

        private String modifierId;

        private LocalDateTime modifyDate;

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