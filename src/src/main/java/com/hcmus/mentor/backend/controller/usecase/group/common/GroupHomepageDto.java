package com.hcmus.mentor.backend.controller.usecase.group.common;

import com.hcmus.mentor.backend.domain.constant.GroupUserRole;
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
    private Date createdDate;
    private Date updatedDate;
    private List<String> mentors = new ArrayList<>();
    private List<String> mentees = new ArrayList<>();
    private String groupCategory;
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;
    private Duration duration;
    private GroupUserRole role;
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

        role = mentors.contains(userId) ? GroupUserRole.MENTOR : GroupUserRole.MENTEE;
    }

    public int getTotalMember() {
        return mentors.size() + mentees.size();
    }
}