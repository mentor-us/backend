package com.hcmus.mentor.backend.service.dto;

import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskResponse;
import com.hcmus.mentor.backend.domain.Meeting;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.service.EventService;
import com.hcmus.mentor.backend.service.EventType;
import lombok.*;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public
class EventDto {
    private String id;
    private String title;
    private String groupName;
    private String user;
    private TaskStatus status;
    private Date timeStart;
    private Date timeEnd;
    private Date deadline;
    private EventType type;

    public static EventDto from(MeetingResponse meeting) {
        return EventDto.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .groupName(meeting.getGroup() == null ? null : meeting.getGroup().getName())
                .user(meeting.getOrganizer() == null ? null : meeting.getOrganizer().getName())
                .timeStart(meeting.getTimeStart())
                .timeEnd(meeting.getTimeEnd())
                .type(EventType.MEETING)
                .build();
    }

    public static EventDto from(Meeting meeting) {
        return EventDto.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .groupName(null)
                .user(null)
                .timeStart(meeting.getTimeStart())
                .timeEnd(meeting.getTimeEnd())
                .type(EventType.MEETING)
                .build();
    }

    public static EventDto from(TaskResponse task) {
        return EventDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .groupName(task.getGroup() == null ? null : task.getGroup().getName())
                .user(task.getAssigner() != null ? task.getAssigner().getName() : null)
                .deadline(task.getDeadline())
                .status(task.getStatus())
                .type(EventType.TASK)
                .build();
    }

    public static EventDto from(Task task) {
        return EventDto.builder()
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
