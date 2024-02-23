package com.hcmus.mentor.backend.controller.payload.response.users;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.UserGender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ProfileResponse implements Serializable {

    private String id;

    private String name;

    private String email;

    private List<String> additionalEmails;

    private String phone;

    private Date birthDate;

    private String personalEmail;

    private UserGender gender;

    private String imageUrl;

    private String wallpaper;

    public static ProfileResponse from(User user) {
        if (user == null) {
            return ProfileResponse.builder().build();
        }

        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .additionalEmails(user.getAdditionalEmails())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .personalEmail(user.getPersonalEmail())
                .phone(user.getPhone())
                .imageUrl(
                        user.getImageUrl().equals("https://graph.microsoft.com/v1.0/me/photo/$value")
                                ? null
                                : user.getImageUrl())
                .wallpaper(user.getWallpaper())
                .build();
    }

    public static ProfileResponse normalize(ProfileResponse response) {
        if (response.getImageUrl() != null
                && response.getImageUrl().equals("https://graph.microsoft.com/v1.0/me/photo/$value")) {
            response.setImageUrl(null);
        }
        return response;
    }

    @Override
    public String toString() {
        return "Người gửi: " + "id='" + id + '\'' + ", tên='" + name + '\'';
    }
}
