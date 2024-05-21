package com.hcmus.mentor.backend.controller.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UpdateTaskRequest {
    private String title;
    private String description;
    private LocalDateTime deadline;
    private List<String> userIds = new ArrayList<>();
    private String parentTask;
}
