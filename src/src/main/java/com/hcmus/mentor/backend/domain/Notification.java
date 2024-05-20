package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "notifications")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type = NotificationType.NEW_MESSAGE;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = new Date();

    @Column(name = "ref_id")
    private String refId;

    @ManyToOne()
    @JoinColumn(name = "sender_id")
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    private User sender;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "notification", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"notification", "user"}, allowSetters = true)
    private List<NotificationUser> receivers = new ArrayList<>();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "title = " + title + ", " +
                "content = " + content + ", " +
                "type = " + type + ", " +
                "createdDate = " + createdDate + ", " +
                "refId = " + refId + ")";
    }
}