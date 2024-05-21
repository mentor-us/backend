package com.hcmus.mentor.backend.service.dto;

import com.hcmus.mentor.backend.domain.Meeting;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.service.EventType;
import lombok.*;

import java.time.LocalDateTime;
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
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;
    private LocalDateTime deadline;
    private EventType type;

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

    public LocalDateTime getUpcomingTime() {
        return (EventType.TASK.equals(getType())) ? getDeadline() : getTimeStart();
    }
}
