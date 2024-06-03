package com.hcmus.mentor.backend.controller.usecase.meeting;

import com.hcmus.mentor.backend.controller.usecase.common.NewEventDto;
import com.hcmus.mentor.backend.controller.usecase.meeting.common.MeetingResult;
import com.hcmus.mentor.backend.controller.usecase.meeting.common.NewMeetingDto;
import com.hcmus.mentor.backend.domain.Meeting;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.service.EventType;
import com.hcmus.mentor.backend.service.dto.MeetingDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class MeetingMapper {

    public MeetingMapper(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Meeting.class, MeetingDto.class).addMappings(mapper -> {
            mapper.map(src -> Optional.ofNullable(src.getOrganizer()).map(User::getId).orElse(null), MeetingDto::setOrganizerId);
            mapper.map(src -> Optional.ofNullable(src.getAttendees()).map(attendees -> attendees.stream().map(User::getId).toList()).orElse(Collections.emptyList()), MeetingDto::setAttendees);
        });

        modelMapper.emptyTypeMap(Meeting.class, NewMeetingDto.class).addMappings(mapper -> {
            mapper.skip(NewMeetingDto::setTimeStart);
            mapper.skip(NewMeetingDto::setTimeEnd);
        }).implicitMappings();

        modelMapper.createTypeMap(NewMeetingDto.class, NewEventDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getGroup().getName(), NewEventDto::setGroupName);
            mapper.map(src -> src.getOrganizer().getName(), NewEventDto::setUser);
            mapper.map(src -> EventType.MEETING, NewEventDto::setType);
            mapper.skip(NewEventDto::setStatus);
            mapper.skip(NewEventDto::setDeadline);
        });

        modelMapper.createTypeMap(Meeting.class, MeetingResult.class).addMappings(mapper -> {
            mapper.map(Meeting::getGroup, MeetingResult::setChannel);
        });
    }
}
