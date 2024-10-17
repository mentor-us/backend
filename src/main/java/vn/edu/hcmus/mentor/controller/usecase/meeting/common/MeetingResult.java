package vn.edu.hcmus.mentor.controller.usecase.meeting.common;

import vn.edu.hcmus.mentor.controller.payload.response.users.ShortProfile;
import vn.edu.hcmus.mentor.controller.usecase.channel.common.ChannelDetailDto;
import vn.edu.hcmus.mentor.domain.MeetingHistory;
import vn.edu.hcmus.mentor.domain.constant.MeetingRepeated;
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