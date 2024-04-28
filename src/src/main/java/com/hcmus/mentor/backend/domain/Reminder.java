package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.ReminderType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Getter
@Setter
@ToString
@Entity
@Builder
@Table(name = "reminders")
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ReminderType type;

    @Column(name = "subject")
    private String subject;

    @Column(name = "content")
    private String name;

    @Column(name = "reminder_date")
    private Date reminderDate;

    @Column(name = "remindable_id")
    private String remindableId;

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "rel_user_reminder",
            joinColumns = @JoinColumn(name = "reminder_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    @ToString.Exclude
    private List<User> recipients;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "channel_id")
    @JsonIgnoreProperties(value = {"lastMessage", "creator", "group", "tasks", "votes", "meetings", "messagesPinned", "users"}, allowSetters = true)
    private Channel group;

    @ElementCollection
    @CollectionTable(name = "reminder_properties", joinColumns = @JoinColumn(name = "reminder_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, Object> propertiesMap;

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return propertiesMap;
    }

    @JsonAnySetter
    public void setProperties(String key, Object value) {
        propertiesMap.put(key, value);
    }
}