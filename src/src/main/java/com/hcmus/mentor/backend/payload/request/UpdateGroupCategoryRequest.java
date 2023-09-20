package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.GroupCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGroupCategoryRequest {
    private String name;

    private String description;

    private Boolean status;
    private String iconUrl;
    private List<GroupCategory.Permission> permissions;
}
