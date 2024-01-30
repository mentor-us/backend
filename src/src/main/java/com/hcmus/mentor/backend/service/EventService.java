package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.Meeting;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskResponse;

import java.util.Date;
import java.util.List;

import lombok.*;

public interface EventService {
    List<Event> getMostRecentEvents(String userId);

    List<Event> getAllOwnEvents(String userId);

    List<Event> mergeEvents(List<Meeting> meetings, List<Task> tasks);

    List<Event> getAllEventsByDate(String userId, Date date);

    List<Event> getAllEventsByMonth(String userId, Date date);

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Builder
    class Event {
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
            return EventService.Event.builder()
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
            return EventService.Event.builder()
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
            return EventService.Event.builder()
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
            return EventService.Event.builder()
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
            return (EventType.TASK.equals(getType())) ? getDeadline() : getTimeStart();
        }
    }
}
