package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.request.groups.UpdateChannelRequest;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
        private ChannelStatus status = ChannelStatus.ACTIVE;

        @Builder.Default
        private ChannelType type = ChannelType.PUBLIC;

        private String creatorId;

        private Boolean hasNewMessage;

        private String imageUrl;

        @Builder.Default
        private List<String> pinnedMessageIds = new ArrayList<>();

        @Builder.Default
        private String parentId = null;

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
