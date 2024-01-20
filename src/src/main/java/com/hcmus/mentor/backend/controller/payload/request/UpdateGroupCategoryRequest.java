package com.hcmus.mentor.backend.controller.payload.request;

import com.hcmus.mentor.backend.domain.GroupCategory;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
