package com.hcmus.mentor.backend.usercase.common.service.impl;

import com.hcmus.mentor.backend.entity.Meeting;
import com.hcmus.mentor.backend.entity.Task;
import com.hcmus.mentor.backend.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskResponse;
import com.hcmus.mentor.backend.usercase.common.repository.MeetingRepository;
import com.hcmus.mentor.backend.usercase.common.repository.TaskRepository;
import com.hcmus.mentor.backend.usercase.common.service.EventService;
import com.hcmus.mentor.backend.usercase.common.service.MeetingService;
import com.hcmus.mentor.backend.usercase.common.service.TaskServiceImpl;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
  public List<Event> getMostRecentEvents(String userId) {
    List<MeetingResponse> meetings = meetingService.getMostRecentMeetings(userId);
    List<TaskResponse> tasks = taskService.getMostRecentTasks(userId);
    return Stream.concat(meetings.stream().map(Event::from), tasks.stream().map(Event::from))
        .filter(event -> event.getUpcomingTime() != null)
        .sorted(Comparator.comparing(Event::getUpcomingTime))
        .limit(5)
        .collect(Collectors.toList());
  }

  @Override
  public List<Event> getAllOwnEvents(String userId) {
    List<Meeting> meetings = meetingService.getAllOwnMeetings(userId);
    List<Task> tasks = taskService.getAllOwnTasks(userId);
    return mergeEvents(meetings, tasks);
  }

  @Override
  public List<Event> mergeEvents(List<Meeting> meetings, List<Task> tasks) {
    return Stream.concat(meetings.stream().map(Event::from), tasks.stream().map(Event::from))
        .filter(event -> event.getUpcomingTime() != null)
        .sorted(Comparator.comparing(Event::getUpcomingTime))
        .collect(Collectors.toList());
  }

  @Override
  public List<Event> getAllEventsByDate(String userId, Date date) {
    List<Meeting> meetings = meetingService.getAllOwnMeetingsByDate(userId, date);
    List<Task> tasks = taskService.getAllOwnTaskByDate(userId, date);
    return mergeEvents(meetings, tasks);
  }

  @Override
  public List<Event> getAllEventsByMonth(String userId, Date date) {
    List<Meeting> meetings = meetingService.getAllOwnMeetingsByMonth(userId, date);
    List<Task> tasks = taskService.getAllOwnTasksByMonth(userId, date);
    return mergeEvents(meetings, tasks);
  }
}
