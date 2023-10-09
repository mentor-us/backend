package com.hcmus.mentor.backend.entity;

import com.hcmus.mentor.backend.payload.request.SubscribeNotificationRequest;
import java.util.Date;
import java.util.List;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
@Document("notification_subscriber")
public class NotificationSubscriber {

  @Id private String id;

  private String userId;

  private String token;

  private List<String> topics;

  @Builder.Default private Date createdDate = new Date();

  public void update(SubscribeNotificationRequest request) {
    this.userId = request.getUserId();
    this.token = request.getToken();
    this.createdDate = new Date();
  }
}
