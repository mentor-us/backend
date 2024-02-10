package com.hcmus.mentor.backend.controller.payload.request;

import com.hcmus.mentor.backend.domain.constant.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

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

    UserRole role;
}
