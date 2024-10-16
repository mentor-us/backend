package com.hcmus.mentor.backend.controller.payload.request.groupcategories;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteGroupCategoryRequest {
    private String newGroupCategoryId;
}