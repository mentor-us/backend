package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.User;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class AddUserRequest {
    @NotBlank
    @Size(min = 1, max = 100)
    String name;
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@(gmail\\.com|hcmus\\.edu\\.vn)$")
    String emailAddress;
    User.Role role;
}
