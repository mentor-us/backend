package com.hcmus.mentor.backend.controller.payload.request.users;

import com.hcmus.mentor.backend.domain.constant.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class AddUserRequest {

    @Size(min = 1, max = 50, message = "Độ dài tên không quá 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z -]+$", message = "Tên chỉ chứa ký tự chữ cái, dấu cách và dấu gạch ngang")
    @NotBlank
    String name;

    @Email(message = "Email không hợp lệ")
    @Size(max = 50, message = "Độ dài email không quá 50 ký tự")
    String emailAddress;

    @NotBlank(message = "Vai trò không được để trống")
    UserRole role;
}