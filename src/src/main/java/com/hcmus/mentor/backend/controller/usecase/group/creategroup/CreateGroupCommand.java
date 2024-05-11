package com.hcmus.mentor.backend.controller.usecase.group.creategroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Command for creating a group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupCommand implements Command<GroupServiceDto> {


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
     * Date when the group is created.
     */
    private Date createdDate;

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
