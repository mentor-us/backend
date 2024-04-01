package com.hcmus.mentor.backend.controller.usecase.channel.getmeetingsbychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.meeting.common.MeetingResult;
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
