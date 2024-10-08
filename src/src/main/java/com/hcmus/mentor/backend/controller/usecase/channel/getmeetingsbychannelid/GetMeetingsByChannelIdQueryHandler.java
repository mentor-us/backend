package com.hcmus.mentor.backend.controller.usecase.channel.getmeetingsbychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.meeting.common.MeetingResult;
import com.hcmus.mentor.backend.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetMeetingsByChannelIdQueryHandler implements Command.Handler<GetMeetingsByChannelIdQuery, List<MeetingResult>> {

    private final ModelMapper modelMapper;
    private final MeetingRepository meetingRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MeetingResult> handle(GetMeetingsByChannelIdQuery query) {
        return meetingRepository.findAllByChannelId(query.getId()).stream()
                .filter(meeting -> !meeting.getIsDeleted())
                .map(meeting -> modelMapper.map(meeting, MeetingResult.class))
                .toList();
    }
}