package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "channels")
public class Channel implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "has_new_message")
    private Boolean hasNewMessage;

    @Builder.Default
    @Column(name = "created_date")
    private Date createdDate = new Date();

    @Builder.Default
    @Column(name = "updated_date")
    private Date updatedDate = new Date();

    @Builder.Default
    @Column(name = "deleted_date")
    private Date deletedDate = null;

    @Builder.Default
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChannelStatus status = ChannelStatus.ACTIVE;

    @Builder.Default
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChannelType type = ChannelType.PUBLIC;

    @Builder.Default
    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;

    @Builder.Default
    @OneToOne(fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    @JoinColumn(name = "last_message_id", referencedColumnName = "id")
    @JsonIgnoreProperties(value = {"channel", "sender", "reply", "vote", "file", "meeting", "task", "reactions"}, allowSetters = true)
    private Message lastMessage = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    @JoinColumn(name = "group_id", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties(value = {"lastMessage", "defaultChannel", "channels", "groupCategory", "creator", "channels", "faqs", "groupUsers"}, allowSetters = true)
    private Group group;

    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"assigner", "group", "parentTask", "subTasks", "assignees"}, allowSetters = true)
    private List<Task> tasks;

    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"creator", "group", "choices"}, allowSetters = true)
    private List<Vote> votes;

    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"organizer", "group", "histories", "attendees"}, allowSetters = true)
    private List<Meeting> meetings;

    @Builder.Default
    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "rel_channel_message_pin", joinColumns = @JoinColumn(name="channel_id"), inverseJoinColumns = @JoinColumn(name = "message_id"))
    @JsonIgnoreProperties(value = {"channel", "sender", "reply", "vote", "file", "meeting", "task", "reactions"}, allowSetters = true)
    private List<Message> messagesPinned = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(name = "rel_user_channel", joinColumns = @JoinColumn(name = "channel_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    private List<User> users = new ArrayList<>();



    public boolean isMember(String userId) {
        return users.stream().anyMatch(user -> user.getId().equals(userId));
    }

    public void ping() {
        updatedDate = new Date();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "description = " + description + ", " +
                "imageUrl = " + imageUrl + ", " +
                "hasNewMessage = " + hasNewMessage + ", " +
                "createdDate = " + createdDate + ", " +
                "updatedDate = " + updatedDate + ", " +
                "deletedDate = " + deletedDate + ", " +
                "status = " + status + ", " +
                "type = " + type + ", " +
                "isPrivate = " + isPrivate + ")";
    }
}