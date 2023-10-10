package com.hcmus.mentor.backend.payload.request;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RescheduleMeetingRequest {

  @NotNull private Date timeStart;

  @NotNull private Date timeEnd;

  private String place;
}
