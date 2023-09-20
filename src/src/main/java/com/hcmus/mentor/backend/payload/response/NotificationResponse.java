package com.hcmus.mentor.backend.payload.response;

import com.hcmus.mentor.backend.entity.Notif;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {

    private String id;

    private String title;

    private String content;

    private Notif.Type type;

    private ShortProfile sender;

    private Date createdDate;

    private String refId;

    public static NotificationResponse from(Notif notif, ShortProfile sender) {
        return NotificationResponse.builder()
                .id(notif.getId())
                .title(notif.getTitle())
                .content(notif.getContent())
                .type(notif.getType())
                .sender(sender)
                .createdDate(notif.getCreatedDate())
                .refId(notif.getRefId())
                .build();
    }
}
