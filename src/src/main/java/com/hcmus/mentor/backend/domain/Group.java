package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.util.DateUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(value = {"lastMessage", "defaultChannel", "channels", "groupCategory", "creator", "channels", "faqs", "groupUsers"}, allowSetters = true)
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
    private Message lastMessage = null;

    /**
     * Default channel identifier
     */
    @BatchSize(size = 10)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "default_channel_id")
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
    private User creator;

    /**
     * List of channel identifiers
     */
    @Builder.Default
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Channel> channels = new HashSet<>();

    /**
     * List of FAQ identifiers
     */
    @Builder.Default
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Faq> faqs = new HashSet<>();

    @Builder.Default
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"group", "user"}, allowSetters = true)
    private Set<GroupUser> groupUsers = new HashSet<>();

    public boolean isMentor(String userId) {
        return groupUsers.stream().anyMatch(member -> member.getUser().getId().equals(userId) && member.isMentor());
    }

    public boolean isMentor(User user) {
        return groupUsers.stream().anyMatch(mentor -> mentor.getId().equals(user.getId()) && mentor.isMentor());
    }

    public boolean isMentee(String userId) {
        return groupUsers.stream().anyMatch(member -> member.getUser().getId().equals(userId) && !member.isMentor());
    }

    public boolean isMentee(User user) {
        return groupUsers.stream().anyMatch(mentee -> mentee.getId().equals(user.getId()) && !mentee.isMentor());
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

    public void ping() {
        this.updatedDate = DateUtils.getCurrentDateAtUTC();
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