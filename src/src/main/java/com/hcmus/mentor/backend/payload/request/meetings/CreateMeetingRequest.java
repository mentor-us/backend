package com.hcmus.mentor.backend.payload.request.meetings;

import com.hcmus.mentor.backend.entity.Meeting;
import lombok.*;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
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
