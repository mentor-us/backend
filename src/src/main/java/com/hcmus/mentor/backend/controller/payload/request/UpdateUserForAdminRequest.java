package com.hcmus.mentor.backend.controller.payload.request;

import com.hcmus.mentor.backend.domain.constant.UserGender;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserForAdminRequest {

    UserRole role;
    private String name;

    @Pattern(regexp = "\\d{10}", message = "Invalid phone number")
    private String phone;

    private LocalDateTime birthDate;

    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Invalid email address")
    private String personalEmail;

    private boolean status;

    @Size(min = 0, max = 1)
    private UserGender gender;
}
