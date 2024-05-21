package com.hcmus.mentor.backend.controller.payload.response;

import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.Notification;
import com.hcmus.mentor.backend.domain.constant.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {

    private String id;

    private String title;

    private String content;

    private NotificationType type;

    private ShortProfile sender;

    private LocalDateTime createdDate;

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
