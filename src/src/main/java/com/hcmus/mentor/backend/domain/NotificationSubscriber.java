package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.request.SubscribeNotificationRequest;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
@Document("notification_subscriber")
public class NotificationSubscriber {

    @Id
    private String id;

    private String userId;

    private String token;

    private List<String> topics;

    @Builder.Default
    private Date createdDate = new Date();

    public void update(SubscribeNotificationRequest request) {
        this.userId = request.getUserId();
        this.token = request.getToken();
        this.createdDate = new Date();
    }
}
