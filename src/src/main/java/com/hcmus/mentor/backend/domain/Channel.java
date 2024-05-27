package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.util.DateUtils;
import jakarta.persistence.*;
import lombok.*;
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
@JsonIgnoreProperties(value = {"lastMessage", "creator", "group", "tasks", "votes", "meetings", "messagesPinned", "users"}, allowSetters = true)
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
    private Date createdDate = DateUtils.getCurrentDateAtUTC();

    @Builder.Default
    @Column(name = "updated_date")
    private Date updatedDate = DateUtils.getCurrentDateAtUTC();

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
    private Message lastMessage = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    @JoinColumn(name = "group_id", referencedColumnName = "id", nullable = false)
    private Group group;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<Task> tasks;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<Vote> votes;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<Meeting> meetings;

    @Builder.Default
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE}, orphanRemoval = true)
    @JoinTable(name = "rel_channel_message_pin", joinColumns = @JoinColumn(name = "channel_id"), inverseJoinColumns = @JoinColumn(name = "message_id"))
    private List<Message> messagesPinned = new ArrayList<>();

    @Builder.Default
    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "rel_user_channel",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"channel_id", "user_id"})})
    private List<User> users = new ArrayList<>();


    public boolean isMember(String userId) {
        return users.stream().anyMatch(user -> user.getId().equals(userId));
    }

    public void ping() {
        updatedDate = DateUtils.getCurrentDateAtUTC();
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