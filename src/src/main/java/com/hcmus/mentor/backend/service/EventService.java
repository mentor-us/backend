package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.entity.Meeting;
import com.hcmus.mentor.backend.entity.Task;
import com.hcmus.mentor.backend.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskResponse;
import com.hcmus.mentor.backend.repository.MeetingRepository;
import com.hcmus.mentor.backend.repository.TaskRepository;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.*;
import org.springframework.stereotype.Service;

@Service
public class EventService {

  private final MeetingService meetingService;

  private final TaskService taskService;

  private final MeetingRepository meetingRepository;

  private final TaskRepository taskRepository;

  public EventService(
      MeetingService meetingService,
      TaskService taskService,
      MeetingRepository meetingRepository,
      TaskRepository taskRepository) {
    this.meetingService = meetingService;
    this.taskService = taskService;
    this.meetingRepository = meetingRepository;
    this.taskRepository = taskRepository;
  }

  public List<Event> getMostRecentEvents(String userId) {
    List<MeetingResponse> meetings = meetingService.getMostRecentMeetings(userId);
    List<TaskResponse> tasks = taskService.getMostRecentTasks(userId);
    return Stream.concat(meetings.stream().map(Event::from), tasks.stream().map(Event::from))
        .filter(event -> event.getUpcomingTime() != null)
        .sorted(Comparator.comparing(Event::getUpcomingTime))
        .limit(5)
        .collect(Collectors.toList());
  }

  public List<Event> getAllOwnEvents(String userId) {
    List<Meeting> meetings = meetingService.getAllOwnMeetings(userId);
    List<Task> tasks = taskService.getAllOwnTasks(userId);
    return mergeEvents(meetings, tasks);
  }

  public List<Event> mergeEvents(List<Meeting> meetings, List<Task> tasks) {
    return Stream.concat(meetings.stream().map(Event::from), tasks.stream().map(Event::from))
        .filter(event -> event.getUpcomingTime() != null)
        .sorted(Comparator.comparing(Event::getUpcomingTime))
        .collect(Collectors.toList());
  }

  public List<Event> getAllEventsByDate(String userId, Date date) {
    List<Meeting> meetings = meetingService.getAllOwnMeetingsByDate(userId, date);
    List<Task> tasks = taskService.getAllOwnTaskByDate(userId, date);
    return mergeEvents(meetings, tasks);
  }

  public List<Event> getAllEventsByMonth(String userId, Date date) {
    List<Meeting> meetings = meetingService.getAllOwnMeetingsByMonth(userId, date);
    List<Task> tasks = taskService.getAllOwnTasksByMonth(userId, date);
    return mergeEvents(meetings, tasks);
  }

  public enum EventType {
    MEETING,
    TASK
  }

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Setter
  @Builder
  public static class Event {
    private String id;
    private String title;
    private String groupName;
    private String user;
    private Task.Status status;
    private Date timeStart;
    private Date timeEnd;
    private Date deadline;
    private EventType type;

    public static Event from(MeetingResponse meeting) {
      return Event.builder()
          .id(meeting.getId())
          .title(meeting.getTitle())
          .groupName(meeting.getGroup() == null ? null : meeting.getGroup().getName())
          .user(meeting.getOrganizer() == null ? null : meeting.getOrganizer().getName())
          .timeStart(meeting.getTimeStart())
          .timeEnd(meeting.getTimeEnd())
          .type(EventType.MEETING)
          .build();
    }

    public static Event from(Meeting meeting) {
      return Event.builder()
          .id(meeting.getId())
          .title(meeting.getTitle())
          .groupName(null)
          .user(null)
          .timeStart(meeting.getTimeStart())
          .timeEnd(meeting.getTimeEnd())
          .type(EventType.MEETING)
          .build();
    }

    public static Event from(TaskResponse task) {
      return Event.builder()
          .id(task.getId())
          .title(task.getTitle())
          .groupName(task.getGroup() == null ? null : task.getGroup().getName())
          .user(task.getAssigner() != null ? task.getAssigner().getName() : null)
          .deadline(task.getDeadline())
          .status(task.getStatus())
          .type(EventType.TASK)
          .build();
    }

    public static Event from(Task task) {
      return Event.builder()
          .id(task.getId())
          .title(task.getTitle())
          .groupName(null)
          .user(null)
          .deadline(task.getDeadline())
          .status(null)
          .type(EventType.TASK)
          .build();
    }

    public Date getUpcomingTime() {
      return (EventService.EventType.TASK.equals(getType())) ? getDeadline() : getTimeStart();
    }
  }
}
