package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.domain.Meeting;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskResponse;
import com.hcmus.mentor.backend.repository.MeetingRepository;
import com.hcmus.mentor.backend.repository.TaskRepository;
import com.hcmus.mentor.backend.service.EventService;
import com.hcmus.mentor.backend.service.MeetingService;
import com.hcmus.mentor.backend.service.TaskServiceImpl;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hcmus.mentor.backend.service.dto.EventDto;
import lombok.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final MeetingService meetingService;
    private final TaskServiceImpl taskService;
    private final MeetingRepository meetingRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<EventDto> getMostRecentEvents(String userId) {
        List<MeetingResponse> meetings = meetingService.getMostRecentMeetings(userId);
        List<TaskResponse> tasks = taskService.getMostRecentTasks(userId);
        return Stream.concat(meetings.stream().map(EventDto::from), tasks.stream().map(EventDto::from))
                .filter(event -> event.getUpcomingTime() != null)
                .sorted(Comparator.comparing(EventDto::getUpcomingTime))
                .limit(5)
                .toList();
    }

    @Override
    public List<EventDto> getAllOwnEvents(String userId) {
        List<Meeting> meetings = meetingService.getAllOwnMeetings(userId);
        List<Task> tasks = taskService.getAllOwnTasks(userId);
        return mergeEvents(meetings, tasks);
    }

    @Override
    public List<EventDto> mergeEvents(List<Meeting> meetings, List<Task> tasks) {
        return Stream.concat(meetings.stream().map(EventDto::from), tasks.stream().map(EventDto::from))
                .filter(event -> event.getUpcomingTime() != null)
                .sorted(Comparator.comparing(EventDto::getUpcomingTime))
                .toList();
    }

    @Override
    public List<EventDto> getAllEventsByDate(String userId, Date date) {
        List<Meeting> meetings = meetingService.getAllOwnMeetingsByDate(userId, date);
        List<Task> tasks = taskService.getAllOwnTaskByDate(userId, date);
        return mergeEvents(meetings, tasks);
    }

    @Override
    public List<EventDto> getAllEventsByMonth(String userId, Date date) {
        List<Meeting> meetings = meetingService.getAllOwnMeetingsByMonth(userId, date);
        List<Task> tasks = taskService.getAllOwnTasksByMonth(userId, date);
        return mergeEvents(meetings, tasks);
    }
}
