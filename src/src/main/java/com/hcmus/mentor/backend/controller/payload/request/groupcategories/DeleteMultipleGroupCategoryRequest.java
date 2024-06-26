package com.hcmus.mentor.backend.controller.payload.request.groupcategories;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMultipleGroupCategoryRequest {
    private List<String> ids;
    private String newGroupCategoryId;
}