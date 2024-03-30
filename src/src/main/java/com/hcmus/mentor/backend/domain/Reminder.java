package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.hcmus.mentor.backend.domain.constant.ReminderType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Data
@Entity
@Builder
@Table(name = "reminders")
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

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "rel_user_reminder",
            joinColumns = @JoinColumn(name = "reminder_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> recipients;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel group;

    @ElementCollection
    @CollectionTable(name = "reminder_properties", joinColumns = @JoinColumn(name = "reminder_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, Object> propertiesMap;

    public Reminder() {

    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return propertiesMap;
    }

    @JsonAnySetter
    public void setProperties(String key, Object value) {
        propertiesMap.put(key, value);
    }

}
