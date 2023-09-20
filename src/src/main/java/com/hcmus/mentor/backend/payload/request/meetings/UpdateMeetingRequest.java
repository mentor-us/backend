package com.hcmus.mentor.backend.payload.request.meetings;

import com.hcmus.mentor.backend.entity.Meeting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMeetingRequest {

    @NotBlank
    @Size(min = 0, max = 20)
    private String title;

    private String description;

    @NotNull
    private Date timeStart;

    @NotNull
    private Date timeEnd;

    @NotNull
    private Meeting.Repeated repeated;

    private String place;

    private String phone;

    @NotNull
    private String organizerId;

    private List<String> attendees;
}
