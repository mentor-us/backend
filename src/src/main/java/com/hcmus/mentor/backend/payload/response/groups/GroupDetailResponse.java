package com.hcmus.mentor.backend.payload.response.groups;

import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.GroupCategory;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
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

    private List<GroupCategory.Permission> permissions;

    private List<String> pinnedMessageIds;

    private List<MessageDetailResponse> pinnedMessages;

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
    }

    public Integer getTotalMember() {
        return mentees.size() + mentors.size();
    }

    public void setRole(String userId) {
        role = mentors.contains(userId) ? "MENTOR" : "MENTEE";
    }

    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
    }
}
