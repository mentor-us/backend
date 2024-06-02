package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.MeetingRepeated;
import com.hcmus.mentor.backend.domain.constant.ReminderType;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meetings")
@JsonIgnoreProperties(value = {"organizer", "group", "histories", "attendees"}, allowSetters = true)
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
    private Date createdDate = com.hcmus.mentor.backend.util.DateUtils.getCurrentDateAtUTC();

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "deleted_date")
    private Date deletedDate = null;

    @BatchSize(size = 10)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @BatchSize(size = 10)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel group;

    @Builder.Default
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "meeting", fetch = FetchType.LAZY)
    private List<MeetingHistory> histories = new ArrayList<>();

    @Builder.Default
    @BatchSize(size = 10)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rel_user_meeting_attendees",
            joinColumns = @JoinColumn(name = "meeting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"meeting_id", "user_id"})})
    private List<User> attendees = new ArrayList<>();


    @Override
    public Reminder toReminder() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm dd/MM/yyyy");
        String formattedTime = sdf.format(DateUtils.addHours(timeStart, 7));

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

    public Date getReminderDate() {
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(timeStart.toInstant(), ZoneId.systemDefault());
        LocalDateTime reminderTime = localDateTime.minusMinutes(30);
        return Date.from(reminderTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}