package vn.edu.hcmus.mentor.controller.payload.request.groupcategories;

import vn.edu.hcmus.mentor.domain.constant.GroupCategoryPermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Tên loại nhóm không được trống")
    @Size(min = 1, max = 100, message = "Tên loại nhóm không được không quá 100 ký tự")
    private String name;

    @Size(max = 200, message = "Mô tả không được không quá 200 ký tự")
    private String description;

    private Boolean status;
    private String iconUrl;
    private List<GroupCategoryPermission> permissions;
}