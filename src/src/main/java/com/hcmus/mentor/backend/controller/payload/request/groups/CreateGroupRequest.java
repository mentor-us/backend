package com.hcmus.mentor.backend.controller.payload.request.groups;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateGroupRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    private String description;

    private Date createdDate;

    private List<String> menteeEmails;

    private List<String> mentorEmails;
    private String groupCategory;
    private Date timeStart;
    private Date timeEnd;
}
