package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.Notif;
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

  private Notif.Type type;

  private String receiverId;

  private Date createdDate;
}
