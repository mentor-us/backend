package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.BatchSize;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group extends BaseDomain implements Serializable {

    /**
     * Map of group status
     */
    @Getter
    private static Map<GroupStatus, String> statusMap = Map.ofEntries(
            Map.entry(GroupStatus.ACTIVE, "Đang hoạt động"),
            Map.entry(GroupStatus.DISABLED, "Bị khoá"),
            Map.entry(GroupStatus.OUTDATED, "Hết thời hạn"),
            Map.entry(GroupStatus.INACTIVE, "Chưa hoạt động"),
            Map.entry(GroupStatus.DELETED, "Đã xóa")
    );

    /**
     * Group identifier
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Group name
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Group description
     */
    @Column(name = "description")
    private String description;

    /**
     * Status of the group
     */
    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GroupStatus status = GroupStatus.ACTIVE;

    /**
     * Avatar image URL
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Flag to indicate if the group has new message
     */
    @Column(name = "has_new_message")
    private Boolean hasNewMessage;

    /**
     * Time start of the group
     */
    @Column(name = "time_start")
    private LocalDateTime timeStart;

    /**
     * Time end of the group
     */
    @Column(name = "time_end")
    private LocalDateTime timeEnd;

    /**
     * Duration of the group
     */
    @Column(name = "duration")
    private Duration duration;

    @Builder.Default
    @BatchSize(size = 10)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id", referencedColumnName = "id")
    @JsonIgnoreProperties(value = {"channel", "sender", "reply", "vote", "file", "meeting", "task", "reactions", "hibernateLazyInitializer", "handler"}, allowSetters = true)
    private Message lastMessage = null;

    /**
     * Default channel identifier
     */
    @BatchSize(size = 10)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "default_channel_id")
    @JsonIgnoreProperties(value = {"lastMessage", "creator", "group", "tasks", "votes", "meetings", "messagesPinned", "users", "hibernateLazyInitializer", "handler"}, allowSetters = true)
    private Channel defaultChannel;

    /**
     * Group category identifier
     */
    @BatchSize(size = 10)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_category_id")
    @JsonIgnoreProperties(value = {"groups", "hibernateLazyInitializer", "handler"}, allowSetters = true)
    private GroupCategory groupCategory;

    @BatchSize(size = 10)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee", "hibernateLazyInitializer", "handler"}, allowSetters = true)
    private User creator;

    /**
     * List of pinned message identifiers
     */
    @Builder.Default
    @BatchSize(size = 10)
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "message_pinned_id")
    @JsonIgnoreProperties(value = {"channel", "sender", "reply", "vote", "file", "meeting", "task", "reactions", "hibernateLazyInitializer", "handler"}, allowSetters = true)
    private List<Message> messagesPinned = new ArrayList<>();

    /**
     * List of channel identifiers
     */
    @Builder.Default
    @JsonIgnore
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"lastMessage", "creator", "group", "tasks", "votes", "meetings", "messagesPinned", "users", "hibernateLazyInitializer", "handler"}, allowSetters = true)
    private List<Channel> channels = new ArrayList<>();

    /**
     * List of FAQ identifiers
     */
    @Builder.Default
    @JsonIgnore
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"creator", "group", "voters"}, allowSetters = true)
    private List<Faq> faqs = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"group", "user"}, allowSetters = true)
    private List<GroupUser> groupUsers = new ArrayList<>();

    public boolean isMentor(String userId) {
        return groupUsers.stream().anyMatch(member -> member.getUser().getId().equals(userId) && member.isMentor());
    }

    public boolean isMentor(User user) {
        return groupUsers.stream().anyMatch(mentor -> mentor.getId().equals(user.getId()));
    }

    public boolean isMember(String userId) {
        return groupUsers.stream().anyMatch(member -> member.getUser().getId().equals(userId));
    }

    public List<User> getMentors() {
        return groupUsers.stream().filter(GroupUser::isMentor).map(GroupUser::getUser).toList();
    }

    public List<User> getMentees() {
        return groupUsers.stream().filter(member -> !member.isMentor()).map(GroupUser::getUser).toList();
    }

    public List<User> getMembers() {
        return groupUsers.stream().map(GroupUser::getUser).toList();
    }

    public void update(
            String name,
            String description,
            GroupStatus status,
            LocalDateTime timeStart,
            LocalDateTime timeEnd,
            GroupCategory groupCategory) {
        if (name != null) {
            this.setName(name);
        }
        if (description != null) {
            this.setDescription(description);
        }
        if (status != null) {
            this.setStatus(status);
        }
        if (timeStart != null) {
            this.setTimeStart(timeStart.with(LocalTime.of(0, 0, 0)));
        }
        if (timeEnd != null) {
            timeStart.with(LocalTime.of(23, 59, 59));
            this.setTimeEnd(timeEnd.with(LocalTime.of(0, 0, 0)));
        }
        if (groupCategory != null) {
            this.setGroupCategory(groupCategory);
        }
    }

    public void ping() {
        this.updatedDate = new Date();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "description = " + description + ", " +
                "status = " + status + ", " +
                "imageUrl = " + imageUrl + ", " +
                "hasNewMessage = " + hasNewMessage + ", " +
                "timeStart = " + timeStart + ", " +
                "timeEnd = " + timeEnd + ", " +
                "duration = " + duration + ", " +
                "createdDate = " + createdDate + ", " +
                "updatedDate = " + updatedDate + ")";
    }
}