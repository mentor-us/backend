package com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace;

import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupWorkspaceResult {

    private String id;
    private String name;
    private String description;
    private Date createdDate;
    private Date updatedDate;
    private List<String> mentors = new ArrayList<>();
    private List<String> mentees = new ArrayList<>();
    private String groupCategory;
    private Date timeStart;
    private Date timeEnd;
    private Duration duration;
    private String role;
    private String imageUrl;
    private List<GroupCategoryPermission> permissions = new ArrayList<>();
    private List<String> pinnedMessageIds;
    private List<MessageDetailResponse> pinnedMessages = new ArrayList<>();
    private List<WorkspaceChannelDto> channels = new ArrayList<>();
    private List<WorkspaceChannelDto> privates = new ArrayList<>();
    private Integer totalMember;
    private String defaultChannelId;

    public void setRole(String userId) {
        if (mentors == null) {
            return;
        }

        role = mentors.contains(userId) ? "MENTOR" : "MENTEE";
    }
}

