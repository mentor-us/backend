package com.hcmus.mentor.backend.payload.request.meetings;

import com.hcmus.mentor.backend.entity.Meeting;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMeetingRequest {

  @NotBlank
  @Size(min = 0, max = 20)
  private String title;

  private String description;

  @NotNull private Date timeStart;

  @NotNull private Date timeEnd;

  @NotNull private Meeting.Repeated repeated;

  private String place;

  private String phone;

  @NotNull private String organizerId;

  private List<String> attendees;
}
