package com.hcmus.mentor.backend.controller.payload.response.groups;

import com.hcmus.mentor.backend.domain.Group;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GroupGeneralResponse {
    private String id;
    private String name;
    private String category;
    private Date lastTimeActive;
    private long totalMessages;
    private long totalTasks;
    private long totalDoneTasks;
    private long totalMeetings;
    private long totalMentees;
    private long totalMentors;
    private Group.Status status;
}
