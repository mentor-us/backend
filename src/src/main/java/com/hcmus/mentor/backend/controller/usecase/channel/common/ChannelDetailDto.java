package com.hcmus.mentor.backend.controller.usecase.channel.common;

import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryPermission;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDetailDto {

    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private Boolean hasNewMessage;
    private Date createdDate = new Date();
    private Date updatedDate = new Date();
    private Date deletedDate = null;
    private ChannelStatus status = ChannelStatus.ACTIVE;
    private ChannelType type = ChannelType.PUBLIC;
    private Boolean isPrivate = false;
    private String lastMessage = "";
    private String creator;
    private String parentId;
    private List<String> members = Collections.emptyList();
    private List<String> mentees = Collections.emptyList();
    private List<String> mentors = Collections.emptyList();
    @Setter(AccessLevel.NONE)
    private String role;
    @Setter(AccessLevel.NONE)
    private int totalMember;
    private String groupCategory;
    private List<GroupCategoryPermission> permissions = new ArrayList<>();
    private List<String> pinnedMessageIds = new ArrayList<>();
    private List<MessageDetailResponse> pinnedMessages = new ArrayList<>();


    public int getTotalMember() {
        return members.size();
    }

    public void setRole(String userId) {
        if (mentors == null) {
            return;
        }

        role = mentors.contains(userId) ? "MENTOR" : "MENTEE";
    }
}