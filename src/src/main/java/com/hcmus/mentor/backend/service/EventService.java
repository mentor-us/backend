package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.Meeting;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.service.dto.EventDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventDto> getAllOwnEvents(String userId);

    List<EventDto> mergeEvents(List<Meeting> meetings, List<Task> tasks);

    List<EventDto> getAllEventsByDate(String userId, LocalDateTime date);

    List<EventDto> getAllEventsByMonth(String userId, LocalDateTime date);

}
