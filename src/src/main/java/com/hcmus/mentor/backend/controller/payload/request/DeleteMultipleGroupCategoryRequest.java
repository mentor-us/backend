package com.hcmus.mentor.backend.controller.payload.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMultipleGroupCategoryRequest {
    private List<String> ids;
    private String newGroupCategoryId;
}
