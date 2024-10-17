package vn.edu.hcmus.mentor.controller.payload.request.users;

import vn.edu.hcmus.mentor.domain.constant.UserRole;
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