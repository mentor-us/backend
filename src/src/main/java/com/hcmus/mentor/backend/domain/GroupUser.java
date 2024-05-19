package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_user")
public class GroupUser implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Builder.Default
    @Column(name = "is_mentor")
    private boolean isMentor = false;

    @Builder.Default
    @Column(name = "is_pinned")
    private boolean isPinned = false;

    @Builder.Default
    @Column(name = "is_marked")
    private boolean isMarked = false;

    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonIgnoreProperties(value = {"lastMessage", "defaultChannel", "channels", "groupCategory", "creator", "channels", "faqs", "groupUsers"}, allowSetters = true)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    private User user;
}