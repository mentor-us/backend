package com.hcmus.mentor.backend.entity;

import com.hcmus.mentor.backend.payload.request.meetings.CreateMeetingRequest;
import com.hcmus.mentor.backend.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.payload.request.meetings.UpdateMeetingRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("meeting")
public class Meeting implements IRemindable, Serializable {

    @Id
    private String id;

    private String title;

    private String description;

    private Date timeStart;

    private Date timeEnd;

    private Repeated repeated;

    private String place;

    private String organizerId;

    @Builder.Default
    private List<String> attendees = new ArrayList<>();

    private String groupId;

    @Builder.Default
    private Date createdDate = new Date();

    @Builder.Default
    private List<MeetingHistory> histories = new ArrayList<>();

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MeetingHistory implements Serializable {

        @Builder.Default
        private String id = UUID.randomUUID().toString();

        private Date timeStart;

        private Date timeEnd;

        private String place;

        private String modifierId;

        @Builder.Default
        private Date modifyDate = new Date();

        @Override
        public String toString() {
            return "\tLịch hẹn: " +
                    "id='" + id + '\'' +
                    ", Thời gian bắt đầu=" + timeStart +
                    ", Thời gian kết thúc=" + timeEnd +
                    ", Địa điểm='" + place + '\'' +
                    ", Ngày cập nhật =" + modifyDate +
                    '\n';
        }
    }

    @Override
    public Reminder toReminder() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", title);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm dd/MM/yyyy");
        String formattedTime = sdf.format(timeStart);
        properties.put("dueDate", formattedTime);
        properties.put("id", id);

        return Reminder.builder()
                .groupId(groupId)
                .name(title)
                .type(Reminder.ReminderType.MEETING)
                .reminderDate(getReminderDate())
                .properties(properties)
                .remindableId(id)
                .build();
    }

    public Date getReminderDate() {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(timeStart.toInstant(), ZoneId.systemDefault());
        LocalDateTime reminderTime = localDateTime.minusMinutes(30);
        return Date.from(reminderTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public enum Repeated {
        NONE,
        ONCE_A_WEEK,
        ONCE_TWO_WEEKS,
        ONCE_A_MONTH,
        EVERY_DAY
    }

    public void create(CreateMeetingRequest request) {
        title = request.getTitle();
        description = request.getDescription();
        timeStart = request.getTimeStart();
        timeEnd = request.getTimeEnd();
        repeated = request.getRepeated();
        place = request.getPlace();
        organizerId = request.getOrganizerId();
        attendees = request.getAttendees();
        groupId = request.getGroupId();

        MeetingHistory creatingEvent = MeetingHistory.builder()
                .timeStart(request.getTimeStart())
                .timeEnd(request.getTimeEnd())
                .place(request.getPlace())
                .modifierId(request.getOrganizerId())
                .build();
        histories = Collections.singletonList(creatingEvent);
    }

    public static Meeting from(CreateMeetingRequest request) {
        Meeting meeting = new Meeting();
        meeting.create(request);
        return meeting;
    }

    public void update(UpdateMeetingRequest request) {
        title = request.getTitle();
        description = request.getDescription();
        timeStart = request.getTimeStart();
        timeEnd = request.getTimeEnd();
        repeated = request.getRepeated();
        place = request.getPlace();
        attendees = request.getAttendees();
    }

    public void reschedule(String modifierId, RescheduleMeetingRequest request) {
        MeetingHistory history = MeetingHistory.builder()
                .timeStart(request.getTimeStart())
                .timeEnd(request.getTimeEnd())
                .place(request.getPlace())
                .modifierId(modifierId)
                .build();
        histories.add(history);

        timeStart = request.getTimeStart();
        timeEnd = request.getTimeEnd();
        place = request.getPlace();
    }

    public void reschedule(String modifierId, UpdateMeetingRequest request) {
        MeetingHistory history = MeetingHistory.builder()
                .timeStart(request.getTimeStart())
                .timeEnd(request.getTimeEnd())
                .place(place)
                .modifierId(modifierId)
                .build();
        histories.add(history);

        timeStart = request.getTimeStart();
        timeEnd = request.getTimeEnd();
        place = request.getPlace();
    }
}
