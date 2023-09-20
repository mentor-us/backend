package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.GroupCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FindGroupCategoryRequest {
    private String name;
    private String description;
    private String status;
}
