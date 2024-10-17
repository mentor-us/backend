package vn.edu.hcmus.mentor.controller.payload.request.groupcategories;

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