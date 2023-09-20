package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.User;
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
    private User.Role role;
}
