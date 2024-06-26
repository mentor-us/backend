package com.hcmus.mentor.backend.controller.payload.response.meetings;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.UserGender;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingAttendeeResponse {

    private String id;

    private String name;

    private String email;

    private String phone;

    private Date birthDate;

    private String personalEmail;

    private UserGender gender;

    private String imageUrl;

    private boolean isMentor;

    public static MeetingAttendeeResponse from(User user, boolean isMentor) {
        if (user == null) {
            return MeetingAttendeeResponse.builder().build();
        }

        return MeetingAttendeeResponse.builder()
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
                .isMentor(isMentor)
                .build();
    }
}
