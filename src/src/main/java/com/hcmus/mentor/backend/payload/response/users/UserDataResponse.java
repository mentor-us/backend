package com.hcmus.mentor.backend.payload.response.users;

import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.payload.request.FindUserRequest;
import lombok.*;

import java.util.Date;

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
