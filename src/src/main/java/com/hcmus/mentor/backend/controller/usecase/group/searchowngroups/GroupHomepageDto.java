package com.hcmus.mentor.backend.controller.usecase.group.searchowngroups;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupHomepageDto {

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

    public void setRole(String userId) {
        if (mentors == null) {
            return;
        }

        role = mentors.contains(userId) ? "MENTOR" : "MENTEE";
    }
}
