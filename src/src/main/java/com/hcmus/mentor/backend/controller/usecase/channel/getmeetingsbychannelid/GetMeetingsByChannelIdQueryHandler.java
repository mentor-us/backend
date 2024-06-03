package com.hcmus.mentor.backend.controller.usecase.channel.getmeetingsbychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.meeting.common.MeetingResult;
import com.hcmus.mentor.backend.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetMeetingsByChannelIdQueryHandler implements Command.Handler<GetMeetingsByChannelIdQuery, List<MeetingResult>> {

    private final MeetingRepository meetingRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MeetingResult> handle(GetMeetingsByChannelIdQuery query) {
        return meetingRepository.findAllByGroupId(query.getId()).stream()
                .map(meeting -> MeetingResult.builder()
                        .id(meeting.getId())
                        .title(meeting.getTitle())
                        .description(meeting.getDescription())
                        .timeStart(meeting.getTimeStart())
                        .timeEnd(meeting.getTimeEnd())
                        .repeated(meeting.getRepeated())
                        .place(meeting.getPlace())
                        .organizer(meeting.getOrganizer())
                        .channel(meeting.getGroup())
                        .histories(meeting.getHistories())
                        .createdDate(meeting.getCreatedDate())
                        .build())
                .toList();
    }
}