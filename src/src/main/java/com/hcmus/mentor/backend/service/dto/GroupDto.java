package com.hcmus.mentor.backend.service.dto;

import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDto {

    private String id;

    private String name;

    private String description;

    @Builder.Default
    private List<String> mentors = new ArrayList<>();

    @Builder.Default

    private List<String> mentees = new ArrayList<>();

    private String groupCategory;

    private String creatorId;

    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVE;

    private String imageUrl;

    @Builder.Default
    private String parentId = null;

    private Boolean hasNewMessage;

    @Builder.Default
    private String lastMessage = null;

    @Builder.Default
    private String lastMessageId = null;

    @Builder.Default
    private List<String> pinnedMessageIds = new ArrayList<>();

    private String defaultChannelId;

    @Builder.Default
    private List<String> channelIds = new ArrayList<>();

    @Builder.Default
    private List<String> privateIds = new ArrayList<>();

    @Builder.Default
    private List<String> faqIds = new ArrayList<>();

    @Builder.Default
    private List<String> markedMenteeIds = new ArrayList<>();

    private LocalDateTime timeStart;

    private LocalDateTime timeEnd;

    private Duration duration;

    @Builder.Default
    private LocalDateTime createdDate  = LocalDateTime.now(ZoneOffset.UTC);

    @Builder.Default
    private LocalDateTime updatedDate = LocalDateTime.now(ZoneOffset.UTC);

    public static GroupDto from(Group group) {
        if (group == null)
            return null;

        return GroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .groupCategory(group.getGroupCategory() == null ? null : group.getGroupCategory().getName())
                .creatorId(group.getCreator() == null ? null : group.getCreator().getId())
                .status(group.getStatus())
                .imageUrl(group.getImageUrl())
                .hasNewMessage(group.getHasNewMessage())
                .lastMessage(group.getLastMessage() == null ? null : group.getLastMessage().getContent())
                .lastMessageId(group.getLastMessage() == null ? null : group.getLastMessage().getId())
                .defaultChannelId(group.getDefaultChannel() == null ? null : group.getDefaultChannel().getId())
                .timeStart(group.getTimeStart())
                .timeEnd(group.getTimeEnd())
                .duration(group.getDuration())
                .createdDate(group.getCreatedDate())
                .updatedDate(group.getUpdatedDate())
                .build();
    }
}