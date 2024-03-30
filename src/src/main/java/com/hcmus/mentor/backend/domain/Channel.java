package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@ToString
@Table(name = "channels")
public class Channel {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
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
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_pinned_id")
    private List<Message> messagesPinned = new ArrayList<>();

    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(mappedBy = "channels", fetch = FetchType.LAZY)
    @JoinTable(name = "rel_user_channel",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users = new ArrayList<>();

    public Channel() {
    }

    public boolean isMember(String userId) {
        return users.stream().anyMatch(user -> user.getId().equals(userId));
    }

    public void ping() {
        updatedDate = new Date();
    }

}
