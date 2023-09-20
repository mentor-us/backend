package com.hcmus.mentor.backend.payload.request.groups;

import com.hcmus.mentor.backend.entity.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
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
