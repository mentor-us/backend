package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.meetings.CreateMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.meetings.UpdateMeetingRequest;
import com.hcmus.mentor.backend.domain.constant.MeetingRepeated;
import com.hcmus.mentor.backend.domain.constant.ReminderType;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.time.DateUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meetings")
public class Meeting implements IRemindable, Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "time_start")
    private Date timeStart;

    @Column(name = "time_end")
    private Date timeEnd;

    @Column(name = "repeated")
    @Enumerated(EnumType.STRING)
    private MeetingRepeated repeated;

    @Column(name = "place")
    private String place;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = new Date();

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "deleted_date")
    private Date deletedDate = null;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @ManyToOne
    @JoinColumn(name = "channel_id")
    private Channel group;

    @Builder.Default
    @OneToMany(mappedBy = "meeting", fetch = FetchType.LAZY)
    private List<MeetingHistory> histories = new ArrayList<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rel_user_meeting_attendees",
            joinColumns = @JoinColumn(name = "meeting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> attendees = new ArrayList<>();

//    public static Meeting from(CreateMeetingRequest request) {
//        Meeting meeting = new Meeting();
//        meeting.create(request);
//        return meeting;
//    }

    @Override
    public Reminder toReminder() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm dd/MM/yyyy");
        String formattedTime = sdf.format(DateUtils.addHours(timeStart, 7));

        Map<String, Object> properties = new HashMap<>();

        properties.put("name", title);
        properties.put("dueDate", formattedTime);
        properties.put("id", id);

        var reminder = Reminder.builder()
                .group(group)
                .name(title)
                .type(ReminderType.MEETING)
                .reminderDate(getReminderDate())
                .build();

        reminder.setPropertiesMap(properties);

        return reminder;
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
//        organizer = request.getOrganizerId();
//        attendees = request.getAttendees();
//        group = request.getGroupId();

        MeetingHistory creatingEvent = MeetingHistory.builder()
                        .timeStart(request.getTimeStart())
                        .timeEnd(request.getTimeEnd())
                        .place(request.getPlace())
//                        .modifier(request.getOrganizerId())
                        .build();
        histories = Collections.singletonList(creatingEvent);
    }

//    public void update(UpdateMeetingRequest request) {
//        title = request.getTitle();
//        description = request.getDescription();
//        timeStart = request.getTimeStart();
//        timeEnd = request.getTimeEnd();
//        repeated = request.getRepeated();
//        place = request.getPlace();
//        attendees = request.getAttendees();
//    }

    public void reschedule(User modifier, RescheduleMeetingRequest request) {
        MeetingHistory history =
                MeetingHistory.builder()
                        .timeStart(request.getTimeStart())
                        .timeEnd(request.getTimeEnd())
                        .place(request.getPlace())
                        .modifier(modifier)
                        .build();
        histories.add(history);

        timeStart = request.getTimeStart();
        timeEnd = request.getTimeEnd();
        place = request.getPlace();
    }

    public void reschedule(User modifier, UpdateMeetingRequest request) {
        MeetingHistory history =
                MeetingHistory.builder()
                        .timeStart(request.getTimeStart())
                        .timeEnd(request.getTimeEnd())
                        .place(place)
                        .modifier(modifier)
                        .build();
        histories.add(history);

        timeStart = request.getTimeStart();
        timeEnd = request.getTimeEnd();
        place = request.getPlace();
    }


}
