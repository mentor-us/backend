package com.hcmus.mentor.backend.entity;

import com.hcmus.mentor.backend.payload.request.groups.UpdateChannelRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
@Document("channel")
public class Channel {

    public static final int MAX_PINNED_MESSAGES = 5;

    @Id
    private String id;

    private String name;

    private String description;

    @Builder.Default
    private Date createdDate = new Date();

    @Builder.Default
    private Date updatedDate = new Date();

    @Builder.Default
    private List<String> userIds = new ArrayList<>();

    @Builder.Default
    private Status status = Status.ACTIVE;

    @Builder.Default
    private Type type = Type.PUBLIC;

    private String creatorId;

    private Boolean hasNewMessage;

    private String imageUrl;

    @Builder.Default
    private List<String> pinnedMessageIds = new ArrayList<>();

    @Builder.Default
    private String parentId = null;

    public enum Status {
        ACTIVE,
        DISABLED,
        OUTDATED,
        INACTIVE,
        DELETED
    }

    public enum Type {
        PUBLIC,
        PRIVATE,
        PRIVATE_MESSAGE
    }

    public boolean isMember(String userId) {
        return userIds.contains(userId);
    }

    public void update(UpdateChannelRequest request) {
        this.name = request.getChannelName();
        this.description = request.getDescription();
        this.type = request.getType();
        this.userIds = request.getUserIds();
    }

    public void normalize() {
        if (pinnedMessageIds == null) {
            pinnedMessageIds = new ArrayList<>();
        }
    }

    public boolean isMaximumPinnedMessages() {
        normalize();
        return pinnedMessageIds.size() >= MAX_PINNED_MESSAGES;
    }

    public void unpinMessage(String messageId) {
        normalize();

        if (!pinnedMessageIds.contains(messageId)) {
            return;
        }
        pinnedMessageIds.remove(messageId);
    }

    public void pinMessage(String messageId) {
        normalize();

        if (pinnedMessageIds.contains(messageId)) {
            return;
        }
        pinnedMessageIds.add(messageId);
    }

    public void ping() {
        updatedDate = new Date();
    }
}
