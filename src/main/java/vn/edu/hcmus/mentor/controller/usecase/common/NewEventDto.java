package vn.edu.hcmus.mentor.controller.usecase.common;

import vn.edu.hcmus.mentor.domain.constant.TaskStatus;
import vn.edu.hcmus.mentor.service.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    private String id;
    private String title;
    private String groupName;
    private String user;
    private TaskStatus status;
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;
    private LocalDateTime deadline;
    private EventType type;

    public LocalDateTime getUpcomingTime() {
        return (EventType.TASK.equals(getType())) ? getDeadline() : getTimeStart();
    }
}
