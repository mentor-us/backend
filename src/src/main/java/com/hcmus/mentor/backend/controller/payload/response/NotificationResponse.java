package com.hcmus.mentor.backend.controller.payload.response;

import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.Notification;
import com.hcmus.mentor.backend.domain.constant.NotificationType;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String id;

    private String title;

    private String content;

    private NotificationType type;

    private ShortProfile sender;

    private Date createdDate;

    private String refId;

    public static NotificationResponse from(Notification notif, ShortProfile sender) {
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
