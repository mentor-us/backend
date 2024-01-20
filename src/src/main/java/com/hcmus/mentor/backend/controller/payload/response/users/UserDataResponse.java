package com.hcmus.mentor.backend.controller.payload.response.users;

import com.hcmus.mentor.backend.domain.User;

import java.util.Date;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDataResponse {
    private String id;
    private String name;
    private String email;
    private boolean status;
    private User.Role role;
    private Boolean emailVerified;
    private Date birthDate;
    private User.Gender gender;
    private String phone;
    private String personalEmail;
}
