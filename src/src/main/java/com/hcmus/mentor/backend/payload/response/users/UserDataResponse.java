package com.hcmus.mentor.backend.payload.response.users;

import com.hcmus.mentor.backend.entity.User;
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
