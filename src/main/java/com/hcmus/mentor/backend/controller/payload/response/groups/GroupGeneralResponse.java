package com.hcmus.mentor.backend.controller.payload.response.groups;

import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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
    private GroupStatus status;
}
