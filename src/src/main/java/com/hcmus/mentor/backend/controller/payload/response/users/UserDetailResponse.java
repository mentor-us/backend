package com.hcmus.mentor.backend.controller.payload.response.users;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.UserGender;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserDetailResponse {

    private String id;

    @Builder.Default
    private String name = "";

    private String email;

    @Builder.Default
    private String imageUrl = "";

    private boolean status;

    private String phone;

    private LocalDateTime birthDate;

    private String personalEmail;

    private UserGender gender;

    @Builder.Default
    private List<GroupInfo> groups = new ArrayList<>();

    private UserRole role;

    public static UserDetailResponse from(User user) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .phone(user.getPhone())
                .imageUrl(
                        user.getImageUrl().equals("https://graph.microsoft.com/v1.0/me/photo/$value")
                                ? null
                                : user.getImageUrl())
                .status(user.isStatus())
                .build();
    }

    public enum Role {
        MENTOR,
        MENTEE
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class GroupInfo {
        private String id;
        private String name;
        private String groupCategory;
        private Role role;
    }
}
