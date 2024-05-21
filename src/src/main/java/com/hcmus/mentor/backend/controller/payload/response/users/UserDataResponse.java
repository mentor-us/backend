package com.hcmus.mentor.backend.controller.payload.response.users;

import com.hcmus.mentor.backend.domain.constant.UserGender;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import lombok.*;

import java.time.LocalDateTime;

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
    private UserRole role;
    private Boolean emailVerified;
    private LocalDateTime birthDate;
    private UserGender gender;
    private String phone;
    private String personalEmail;
}
