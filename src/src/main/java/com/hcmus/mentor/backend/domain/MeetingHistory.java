package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.cglib.core.Local;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Data
@Entity
@Builder
@Table(name = "meeting_histories")
@AllArgsConstructor
public class MeetingHistory implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "time_start")
    private LocalDateTime timeStart;

    @Column(name = "time_end")
    private LocalDateTime timeEnd;

    @Column(name = "place")
    private String place;

    @Builder.Default
    @Column(name = "modify_date", nullable = false)
    private LocalDateTime modifyDate = LocalDateTime.now(ZoneOffset.UTC);

    @ManyToOne
    @JoinColumn(name = "modifier_id")
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    private User modifier;

    @ManyToOne
    @JoinColumn(name = "meeting_id")
    @JsonIgnoreProperties(value = {"organizer", "group", "histories", "attendees"}, allowSetters = true)
    private Meeting meeting;

    public MeetingHistory() {

    }

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
