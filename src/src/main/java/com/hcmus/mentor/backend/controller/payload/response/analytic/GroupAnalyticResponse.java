package com.hcmus.mentor.backend.controller.payload.response.analytic;

import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GroupAnalyticResponse {
    private String id;
    private String name;
    private String category;
    private Date lastTimeActive;
    private long totalMessages;
    private long totalTasks;
    private long totalMeetings;
    private long totalMentees;
    private long totalMentors;
    private GroupStatus status;
    private String imageUrl;

    private List<Member> members;

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class Member {
        private String id;
        private String email;
        private String name;
        private int trainingPoint;
        private Boolean hasEnglishCert;
        private Double studyingPoint;
        private String role;
        private long totalMessages;
        private long totalMeetings;
        private long totalTasks;
        private long totalDoneTasks;
        private Date lastTimeActive;
    }
}