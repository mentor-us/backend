package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.User;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserForAdminRequest {

  User.Role role;
  private String name;

  @Pattern(regexp = "\\d{10}", message = "Invalid phone number")
  private String phone;

  private Date birthDate;

  @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Invalid email address")
  private String personalEmail;

  private boolean status;

  @Size(min = 0, max = 1)
  private User.Gender gender;
}
