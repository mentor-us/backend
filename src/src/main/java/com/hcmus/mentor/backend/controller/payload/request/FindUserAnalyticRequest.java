package com.hcmus.mentor.backend.controller.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FindUserAnalyticRequest {
    private String name;
    private String email;
    private Role role;
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;

    public enum Role {
        MENTOR,
        MENTEE
    }
}
