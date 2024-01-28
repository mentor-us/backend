package com.hcmus.mentor.backend.controller.payload.request.meetings;

import com.hcmus.mentor.backend.domain.Meeting;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateMeetingRequest {

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

    @NotNull
    private String place;

    @NotNull
    private String organizerId;

    private List<String> attendees;

    @NotNull
    private String groupId;
}