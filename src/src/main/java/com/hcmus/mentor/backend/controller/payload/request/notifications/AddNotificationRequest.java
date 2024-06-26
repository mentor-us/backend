package com.hcmus.mentor.backend.controller.payload.request.notifications;

import com.hcmus.mentor.backend.domain.constant.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AddNotificationRequest {

    private String title;

    private String content;

    private NotificationType type;

    private String receiverId;

    private Date createdDate;
}