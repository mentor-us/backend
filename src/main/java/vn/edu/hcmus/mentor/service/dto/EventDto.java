package vn.edu.hcmus.mentor.service.dto;

import vn.edu.hcmus.mentor.domain.Meeting;
import vn.edu.hcmus.mentor.domain.Task;
import vn.edu.hcmus.mentor.domain.constant.TaskStatus;
import vn.edu.hcmus.mentor.service.EventType;
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

    public Date getUpcomingTime() {
        return (EventType.TASK.equals(getType())) ? getDeadline() : getTimeStart();
    }
}
