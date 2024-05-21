package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.MeetingRepeated;
import com.hcmus.mentor.backend.domain.constant.ReminderType;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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
    private LocalDateTime timeStart;

    @Column(name = "time_end")
    private LocalDateTime timeEnd;

    @Column(name = "repeated")
    @Enumerated(EnumType.STRING)
    private MeetingRepeated repeated;

    @Column(name = "place")
    private String place;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now(ZoneOffset.UTC);

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "deleted_date")
    private LocalDateTime deletedDate = null;

    @BatchSize(size = 10)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    private User organizer;

    @BatchSize(size = 10)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    @JsonIgnoreProperties(value = {"lastMessage", "creator", "group", "tasks", "votes", "meetings", "messagesPinned", "users"}, allowSetters = true)
    private Channel group;

    @JsonIgnore
    @Builder.Default
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "meeting", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"meeting", "modifier"}, allowSetters = true)
    private List<MeetingHistory> histories = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(name = "rel_user_meeting_attendees", joinColumns = @JoinColumn(name = "meeting_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    private List<User> attendees = new ArrayList<>();


    @Override
    public Reminder toReminder() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm dd/MM/yyyy");
        String formattedTime = dateTimeFormatter.format(timeStart.plusHours(7));

        var reminder = Reminder.builder()
                .group(group)
                .name(title)
                .type(ReminderType.MEETING)
                .reminderDate(getReminderDate())
                .remindableId(id)
                .build();

        reminder.setProperties("name", title);
        reminder.setProperties("dueDate", formattedTime);
        reminder.setProperties("id", id);

        return reminder;
    }

    public LocalDateTime getReminderDate() {
        return timeStart.minusMinutes(30);
    }
}