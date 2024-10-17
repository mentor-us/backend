package vn.edu.hcmus.mentor.controller.usecase.channel.getmeetingsbychannelid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.usecase.meeting.common.MeetingResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class GetMeetingsByChannelIdQuery implements Command<List<MeetingResult>> {

    /**
     * The ID of the channel to retrieve.
     */
    private String id;
}
