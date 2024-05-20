package com.hcmus.mentor.backend.controller.payload.response.groups;

import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryPermission;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class GroupDetailResponse {

    private String id;

    private String name;

    private String description;

    private Date createdDate;

    private Date updatedDate;

    private List<String> mentors;

    private List<String> mentees;

    private String groupCategory;

    private LocalDateTime timeStart;

    private LocalDateTime timeEnd;

    private Duration duration;

    private String role;

    private boolean isPinned;

    private String imageUrl;

    private List<GroupCategoryPermission> permissions;

    private List<String> pinnedMessageIds;

    private List<MessageDetailResponse> pinnedMessages;

    private List<GroupChannel> channels;

    private List<GroupChannel> privates;

    private Integer totalMember;

    private String parentId;

    @Builder.Default
    private ChannelType type = ChannelType.PUBLIC;

    private String defaultChannelId;

    public GroupDetailResponse(Group group) {
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.createdDate = group.getCreatedDate();
        this.updatedDate = group.getUpdatedDate();
        this.mentors = group.getMentors().stream().map(User::getId).toList();
        this.mentees = group.getMentees().stream().map(User::getId).toList();
        this.groupCategory = group.getGroupCategory().getId();
        this.timeStart = group.getTimeStart();
        this.timeEnd = group.getTimeEnd();
        this.duration = group.getDuration();
        this.imageUrl = group.getImageUrl();
        this.permissions = group.getGroupCategory().getPermissions();
        this.defaultChannelId = group.getDefaultChannel().getId();
    }

    public GroupDetailResponse(Group group, List<GroupChannel> channels, List<GroupChannel> privates) {
        this(group);
        this.channels = channels;
        this.privates = privates;
    }

    public Integer getTotalMember() {
        if (mentees == null || mentors == null) {
            return totalMember;
        }
        return mentees.size() + mentors.size();
    }

    public void setTotalMember(int totalMember) {
        this.totalMember = totalMember;
    }

    public void setRole(String userId) {
        if (mentors == null) {
            return;
        }
        role = mentors.contains(userId) ? "MENTOR" : "MENTEE";
    }

    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
    }

    public void removePermission(GroupCategoryPermission permission) {
        if (!permissions.contains(permission)) {
            return;
        }
        permissions.remove(permission);
        setPermissions(permissions);
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Builder
    public static class GroupChannel {

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

        private String newMessage;

        private String newMessageId;

        private String imageUrl;

        @Builder.Default
        private List<String> pinnedMessageIds = new ArrayList<>();

        @Builder.Default
        private String parentId = null;

        private Boolean marked;

        public static GroupChannel from(Channel channel) {
            return GroupChannel.builder()
                    .id(channel.getId())
                    .name(channel.getName())
                    .description(channel.getDescription())
                    .createdDate(channel.getCreatedDate())
                    .updatedDate(channel.getUpdatedDate())
                    .userIds(channel.getUsers().stream().map(User::getId).toList())
                    .status(channel.getStatus())
                    .type(channel.getType())
                    .creatorId(channel.getCreator().getId())
                    .hasNewMessage(channel.getHasNewMessage())
                    .imageUrl(channel.getImageUrl())
                    .pinnedMessageIds(channel.getMessagesPinned().stream().map(m -> m.getId()).toList())
                    .parentId(channel.getGroup().getId())
                    .newMessageId(channel.getLastMessage() == null ? null : channel.getLastMessage().getId())
                    .build();
        }

        public static GroupChannel from(Channel channel, boolean marked) {
            GroupChannel groupChannel = from(channel);
            groupChannel.setMarked(marked);
            return groupChannel;
        }
    }
}