package com.hcmus.mentor.backend.controller.payload.response.groups;

import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupHomepageResponse {

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

    private boolean pinned;

    private String imageUrl;

    private Boolean hasNewMessage;

    private String newMessage;

    private String newMessageId;

    private String defaultChannelId;

    public GroupHomepageResponse(Group group, String role, boolean isPinned) {
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
        this.role = role;
        this.imageUrl = group.getImageUrl();
        this.pinned = isPinned;
        this.hasNewMessage = group.getHasNewMessage();
        this.newMessage = group.getLastMessage() != null ? group.getLastMessage().getContent() : null;
        this.newMessageId = group.getLastMessage() != null ? group.getLastMessage().getId() : null;
        this.defaultChannelId = group.getDefaultChannel() != null ? group.getDefaultChannel().getId() : null;
    }

    public Integer getTotalMember() {
        return mentees.size() + mentors.size();
    }
}
