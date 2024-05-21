package com.hcmus.mentor.backend.controller.usecase.group.common;

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
public class GroupHomepageDto {

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
    private boolean pinned;
    private String imageUrl;
    private Boolean hasNewMessage;
    private String newMessage;
    private String newMessageId;
    private String defaultChannelId;
    @Setter(AccessLevel.NONE)
    private int totalMember;

    public void setRole(String userId) {
        if (mentors == null) {
            return;
        }

        role = mentors.contains(userId) ? "MENTOR" : "MENTEE";
    }

    public int getTotalMember() {
        return mentors.size() + mentees.size();
    }
}