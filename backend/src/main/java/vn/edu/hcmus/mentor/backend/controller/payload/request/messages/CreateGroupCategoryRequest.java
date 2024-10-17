package vn.edu.hcmus.mentor.backend.controller.payload.request.messages;

import vn.edu.hcmus.mentor.backend.domain.constant.GroupCategoryPermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateGroupCategoryRequest {

    @NotBlank(message = "Tên loại nhóm không được trống")
    @Size(min = 1, max = 100, message = "Tên loại nhóm không được không quá 100 ký tự")
    private String name;

    @Size(max = 200, message = "Mô tả không được không quá 200 ký tự")
    private String description;

    private String iconUrl;
    private List<GroupCategoryPermission> permissions;
}