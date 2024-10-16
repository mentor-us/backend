package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
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
@JsonIgnoreProperties(value = {"recipients", "group"}, allowSetters = true)
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rel_user_reminder",
            joinColumns = @JoinColumn(name = "reminder_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"reminder_id", "user_id"})}
    )
    @ToString.Exclude
    private List<User> recipients;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    @ToString.Exclude
    private Channel group;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "reminder_properties", joinColumns = @JoinColumn(name = "reminder_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value", columnDefinition = "varchar(255)")
    private Map<String, String> propertiesMapJson = new java.util.HashMap<>();

    @JsonAnyGetter
    @SneakyThrows
    public Map<String, Object> getProperties() {
        return new java.util.HashMap<>(propertiesMapJson);
    }

    @SneakyThrows
    public void setProperties(String key, Object value) {
        this.propertiesMapJson.put(key, value.toString());
    }
}