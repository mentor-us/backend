package com.hcmus.mentor.backend.controller.usecase.meeting.common;

import com.hcmus.mentor.backend.domain.Meeting;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.service.dto.MeetingDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class MeetingMapper {

    public MeetingMapper(ModelMapper modelMapper) {

        modelMapper.createTypeMap(Meeting.class, MeetingDto.class).addMappings(mapper -> {

            mapper.map(src -> Optional.ofNullable(src.getOrganizer()).map(User::getId).orElse(null),
                    MeetingDto::setOrganizerId);

            mapper.map(src -> Optional.ofNullable(src.getAttendees())
                            .map(attendees -> attendees.stream().map(User::getId).toList())
                            .orElse(Collections.emptyList()),
                    MeetingDto::setAttendees);
        });
    }
}