package com.hcmus.mentor.backend.controller.usecase.group.common;

import com.hcmus.mentor.backend.domain.constant.GroupCategoryPermission;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.domain.constant.GroupUserRole;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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
    private GroupUserRole role;
    private List<GroupCategoryPermission> permissions = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    private boolean stopWorking;
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;

    @Setter(AccessLevel.NONE)
    private int totalMember;
    private LocalDateTime updatedDate;

    public boolean getStopWorking() {
        return Arrays.asList(GroupStatus.DISABLED, GroupStatus.INACTIVE, GroupStatus.DELETED).contains(status);
    }

    public int getTotalMember() {
        return members.size();
    }

    public void setRole(String userId) {
        if (mentors == null) {
            return;
        }
        role = mentors.contains(userId) ? GroupUserRole.MENTOR : GroupUserRole.MENTEE;
    }
}
