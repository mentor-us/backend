package com.hcmus.mentor.backend.controller.payload.request.groupcategories;

import com.hcmus.mentor.backend.domain.constant.GroupCategoryPermission;
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
    private List<GroupCategoryPermission> permissions;
}