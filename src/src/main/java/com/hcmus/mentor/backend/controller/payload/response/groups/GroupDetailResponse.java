package com.hcmus.mentor.backend.controller.payload.response.groups;

import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.domain.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryPermission;
import lombok.*;
import org.springframework.data.annotation.Id;

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

    private Date timeStart;

    private Date timeEnd;

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

    private ChannelType type = ChannelType.PUBLIC;

    public GroupDetailResponse(Group group) {
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.createdDate = group.getCreatedDate();
        this.updatedDate = group.getUpdatedDate();
        this.mentors = group.getMentors();
        this.mentees = group.getMentees();
        this.timeStart = group.getTimeStart();
        this.timeEnd = group.getTimeEnd();
        this.duration = group.getDuration();
        this.imageUrl = group.getImageUrl();
        this.parentId = group.getParentId();
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
                    .userIds(channel.getUserIds())
                    .status(channel.getStatus())
                    .type(channel.getType())
                    .creatorId(channel.getCreatorId())
                    .hasNewMessage(channel.getHasNewMessage())
                    .imageUrl(channel.getImageUrl())
                    .pinnedMessageIds(channel.getPinnedMessageIds())
                    .parentId(channel.getParentId())
                    .build();
        }

        public static GroupChannel from(Channel channel, boolean marked) {
            GroupChannel groupChannel = from(channel);
            groupChannel.setMarked(marked);
            return groupChannel;
        }
    }
}
