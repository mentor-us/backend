package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.GroupCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateGroupCategoryRequest {
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;
    @Size(max = 200)
    private String description;
    private String iconUrl;
    private List<GroupCategory.Permission> permissions;
}
