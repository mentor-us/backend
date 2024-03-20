package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

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
    private String id;

    private String name;

    private String description;

    @Builder.Default
    private Date createdDate = new Date();

    @Builder.Default
    private Date updatedDate = new Date();

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "user_id")
    private List<User> users = new ArrayList<>();

    @Builder.Default
    private ChannelStatus status = ChannelStatus.ACTIVE;

    @Builder.Default
    private ChannelType type = ChannelType.PUBLIC;

    private String creatorId;

    private Boolean hasNewMessage;

    private String imageUrl;

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "message_id")
    private List<Message> pinnedMessageIds = new ArrayList<>();

    @Builder.Default
    private String parentId = null;

    public Channel() {
    }

    public boolean isMember(String userId) {
        return users.contains(userId);
    }

    public void ping() {
        updatedDate = new Date();
    }

}
