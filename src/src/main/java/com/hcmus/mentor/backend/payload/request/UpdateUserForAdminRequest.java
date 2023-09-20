package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.User;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserForAdminRequest {

    private String name;
    @Pattern(regexp = "\\d{10}", message = "Invalid phone number")
    private String phone;
    private Date birthDate;
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Invalid email address")
    private String personalEmail;
    private boolean status;
    User.Role role;
    @Size(min = 0, max = 1)
    private User.Gender gender;
}