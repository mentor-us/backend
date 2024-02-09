package com.hcmus.mentor.backend.controller.payload.request;

import com.hcmus.mentor.backend.domain.Notify;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AddNotificationRequest {

    private String title;

    private String content;

    private Notify.Type type;

    private String receiverId;

    private Date createdDate;
}
