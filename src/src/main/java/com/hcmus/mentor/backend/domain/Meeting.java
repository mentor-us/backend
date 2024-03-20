package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.meetings.CreateMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.meetings.UpdateMeetingRequest;
import com.hcmus.mentor.backend.domain.constant.MeetingRepeated;
import com.hcmus.mentor.backend.domain.constant.ReminderType;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import lombok.*;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.annotation.Id;

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

    private MeetingRepeated repeated;

    private String place;

    private String organizerId;

    @Builder.Default
    private List<String> attendees = new ArrayList<>();

    private String groupId;

    @Builder.Default
    private Date createdDate = new Date();

    @Builder.Default
    private List<MeetingHistory> histories = new ArrayList<>();

    public static Meeting from(CreateMeetingRequest request) {
        Meeting meeting = new Meeting();
        meeting.create(request);
        return meeting;
    }

    @Override
    public Reminder toReminder() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm dd/MM/yyyy");
        String formattedTime = sdf.format(DateUtils.addHours(timeStart, 7));

        Map<String, Object> properties = new HashMap<>();

        properties.put("name", title);
        properties.put("dueDate", formattedTime);
        properties.put("id", id);

        return Reminder.builder()
                .groupId(groupId)
                .name(title)
                .type(ReminderType.MEETING)
                .reminderDate(getReminderDate())
                .properties(properties)
                .remindableId(id)
                .build();
    }

    public Date getReminderDate() {
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(timeStart.toInstant(), ZoneId.systemDefault());
        LocalDateTime reminderTime = localDateTime.minusMinutes(30);
        return Date.from(reminderTime.atZone(ZoneId.systemDefault()).toInstant());
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

        MeetingHistory creatingEvent =
                MeetingHistory.builder()
                        .timeStart(request.getTimeStart())
                        .timeEnd(request.getTimeEnd())
                        .place(request.getPlace())
                        .modifierId(request.getOrganizerId())
                        .build();
        histories = Collections.singletonList(creatingEvent);
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
        MeetingHistory history =
                MeetingHistory.builder()
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
        MeetingHistory history =
                MeetingHistory.builder()
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
            return "\tLịch hẹn: "
                    + "id='"
                    + id
                    + '\''
                    + ", Thời gian bắt đầu="
                    + timeStart
                    + ", Thời gian kết thúc="
                    + timeEnd
                    + ", Địa điểm='"
                    + place
                    + '\''
                    + ", Ngày cập nhật ="
                    + modifyDate
                    + '\n';
        }
    }
}
