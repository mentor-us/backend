package com.hcmus.mentor.backend.controller.payload.request.groups;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Command for creating a group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {

    /**
     * Name of the group.
     */
    @NotBlank
    @Max(100)
    private String name;

    /**
     * Description of the group
     */
    private String description;

    /**
     * LocalDateTime when the group is created.
     */
    private LocalDateTime createdDate;

    /**
     * List of mentee emails associated with the group.
     */
    private List<String> menteeEmails;

    /**
     * List of mentor emails associated with the group.
     */
    private List<String> mentorEmails;

    /**
     * category of the group.
     */
    private String groupCategory;

    /**
     * Start time of the group.
     */
    @NotNull
    private LocalDateTime timeStart;

    /**
     * End time of the group.
     */
    @NotNull
    private LocalDateTime timeEnd;
}
