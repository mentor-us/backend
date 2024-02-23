package com.hcmus.mentor.backend.controller.payload.request.groups;

import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGroupRequest {
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @Size(max = 200)
    private String description;

    private GroupStatus status;
    private Date timeStart;
    private Date timeEnd;
    private String groupCategory;
}
