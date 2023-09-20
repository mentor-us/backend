package com.hcmus.mentor.backend.payload.response.users;

import com.hcmus.mentor.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserDetailResponse {

    private String id;

    private String name = "";

    private String email;

    private String imageUrl = "";

    private boolean status;

    private String phone;

    private Date birthDate;

    private String personalEmail;

    private User.Gender gender;

    @Builder.Default
    private List<GroupInfo> groups = new ArrayList<>();

    private User.Role role;

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class GroupInfo{
        private String id;
        private String name;
        private String groupCategory;
        private Role role;
    }
    public enum Role {
        MENTOR,
        MENTEE
    }
    public static UserDetailResponse from(User user) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .personalEmail(user.getPersonalEmail())
                .phone(user.getPhone())
                .imageUrl(user.getImageUrl().equals("https://graph.microsoft.com/v1.0/me/photo/$value")
                        ? null : user.getImageUrl())
                .status(user.isStatus())
                .build();
    }
}
