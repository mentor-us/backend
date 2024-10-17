package vn.edu.hcmus.mentor.backend.controller.payload.response.meetings;

import vn.edu.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import vn.edu.hcmus.mentor.backend.domain.MeetingHistory;
import lombok.*;

import java.util.Date;

@Getter
@Setter
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