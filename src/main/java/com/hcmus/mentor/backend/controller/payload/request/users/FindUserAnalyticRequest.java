package com.hcmus.mentor.backend.controller.payload.request.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FindUserAnalyticRequest {
    private String name;
    private String email;
    private Role role;
    private Date timeStart;
    private Date timeEnd;

    public enum Role {
        MENTOR,
        MENTEE
    }
}