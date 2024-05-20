package com.hcmus.mentor.backend.controller.payload.response.analytic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SystemAnalyticChartResponse {
    @Builder.Default
    private List<MonthSystemAnalytic> data = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class MonthSystemAnalytic {
        private int month;
        private int year;
        private long newGroups;
        private long newMessages;
        private long newTasks;
        private long newMeetings;
        private long newUsers;
    }
}
