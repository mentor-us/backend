package com.hcmus.mentor.backend.controller.usecase.group.searchgroup;

import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDetailDto {

    private String id;
    private List<String> channelIds = Collections.emptyList();
    private Date createdDate;
    private String creatorId;
    private String defaultChannelId;
    private String description;
    private Duration duration;
    private List<String> fagIds = Collections.emptyList();
    private String groupCategory;
    private boolean hasNewMessage;
    private String imageUrl;
    private String lastMessage;
    private String lastMessageId;
    private boolean maximumPinnedMessages;
    private List<String> members = Collections.emptyList();
    private List<String> mentees = Collections.emptyList();
    private List<String> mentors = Collections.emptyList();
    private String name;
    private GroupStatus status;

    @Setter(AccessLevel.NONE)
    private boolean stopWorking;
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;

    @Setter(AccessLevel.NONE)
    private int totalMembers;
    private LocalDateTime updatedDate;

    public boolean getStopWorking() {
        return Arrays.asList(GroupStatus.DISABLED, GroupStatus.INACTIVE, GroupStatus.DELETED).contains(status);
    }

    public int getTotalMembers() {
        return members.size();
    }
}
