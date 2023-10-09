package com.hcmus.mentor.backend.payload.request;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FindUserAnalyticRequest {
  private String name;
  private String email;
  private Role role;
  private Date timeStart;
  private Date timeEnd;

  public enum Role {
    MENTOR,
    MENTEE
  }
}
