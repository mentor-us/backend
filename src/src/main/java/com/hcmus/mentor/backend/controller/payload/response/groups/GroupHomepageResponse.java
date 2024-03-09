package com.hcmus.mentor.backend.controller.payload.response.groups;

import com.hcmus.mentor.backend.domain.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
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

    private Date timeStart;

    private Date timeEnd;

    private Duration duration;

    private String role;

    private boolean pinned;

    private String imageUrl;

    private Boolean hasNewMessage;

    private String newMessage;

    private String newMessageId;

    private String defaultChannelId;

    public GroupHomepageResponse(Group group, String groupCategory, String role) {
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.createdDate = group.getCreatedDate();
        this.updatedDate = group.getUpdatedDate();
        this.mentors = group.getMentors();
        this.mentees = group.getMentees();
        this.groupCategory = groupCategory;
        this.timeStart = group.getTimeStart();
        this.timeEnd = group.getTimeEnd();
        this.duration = group.getDuration();
        this.role = role;
        this.imageUrl = group.getImageUrl();
        this.hasNewMessage = group.getHasNewMessage();
        this.newMessage = group.getLastMessage();
        this.newMessageId = group.getLastMessageId();
        this.defaultChannelId = group.getDefaultChannelId();
    }

    public static GroupHomepageResponse from(Group group, String groupCategory, String role) {
        return new GroupHomepageResponse(group, groupCategory, role);
    }

    public static GroupHomepageResponse from(Group group, String groupCategory) {
        return new GroupHomepageResponse(group, groupCategory, null);
    }

    public Integer getTotalMember() {
        return mentees.size() + mentors.size();
    }
}
