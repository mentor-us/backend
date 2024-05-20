package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.Meeting;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.service.dto.EventDto;

import java.util.Date;
import java.util.List;

public interface EventService {

    List<EventDto> getAllOwnEvents(String userId);

    List<EventDto> mergeEvents(List<Meeting> meetings, List<Task> tasks);

    List<EventDto> getAllEventsByDate(String userId, Date date);

    List<EventDto> getAllEventsByMonth(String userId, Date date);

}
