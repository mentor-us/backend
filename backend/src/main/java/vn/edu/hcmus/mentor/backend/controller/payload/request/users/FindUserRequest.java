package vn.edu.hcmus.mentor.backend.controller.payload.request.users;

import vn.edu.hcmus.mentor.backend.domain.constant.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FindUserRequest {
    private String name;
    private String email;
    private Boolean status;
    private UserRole role;
}