package com.hcmus.mentor.backend.controller.usecase.group.common;

import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryPermission;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupWorkspaceDto {

    private String id;
    private String name;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<String> mentors = new ArrayList<>();
    private List<String> mentees = new ArrayList<>();
    private String groupCategory;
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;
    private Duration duration;
    private String role;
    private String imageUrl;
    private List<GroupCategoryPermission> permissions = new ArrayList<>();
    private List<String> pinnedMessageIds = new ArrayList<>();
    private List<MessageDetailResponse> pinnedMessages = new ArrayList<>();
    private List<WorkspaceChannelDto> channels = new ArrayList<>();
    private List<WorkspaceChannelDto> privates = new ArrayList<>();
    @Setter(AccessLevel.NONE)
    private Integer totalMember;
    private String defaultChannelId;

    public Integer getTotalMember() {
        return mentors.size() + mentees.size();
    }

    public void setRole(String userId) {
        if (mentors == null) {
            return;
        }

        role = mentors.contains(userId) ? "MENTOR" : "MENTEE";
    }
}

