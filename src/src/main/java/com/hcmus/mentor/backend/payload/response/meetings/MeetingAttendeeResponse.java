package com.hcmus.mentor.backend.payload.response.meetings;

import com.hcmus.mentor.backend.entity.User;
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

    private User.Gender gender;

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
                .personalEmail(user.getPersonalEmail())
                .phone(user.getPhone())
                .imageUrl(user.getImageUrl().equals("https://graph.microsoft.com/v1.0/me/photo/$value")
                        ? null : user.getImageUrl())
                .isMentor(isMentor)
                .build();
    }
}
