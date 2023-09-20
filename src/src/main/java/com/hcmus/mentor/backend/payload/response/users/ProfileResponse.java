package com.hcmus.mentor.backend.payload.response.users;

import com.hcmus.mentor.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ProfileResponse implements Serializable {

    private String id;

    private String name;

    private String email;

    private String phone;

    private Date birthDate;

    private String personalEmail;

    private User.Gender gender;

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
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .personalEmail(user.getPersonalEmail())
                .phone(user.getPhone())
                .imageUrl(user.getImageUrl().equals("https://graph.microsoft.com/v1.0/me/photo/$value")
                        ? null : user.getImageUrl())
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
        return "Người gửi: " +
                "id='" + id + '\'' +
                ", tên='" + name + '\'';
    }
}
