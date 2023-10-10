package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
  @NotNull private String name;
  private String imageUrl;
  private String phone;
  private Date birthDate;
  private String personalEmail;

  @Size(min = 0, max = 1)
  private User.Gender gender;
}
