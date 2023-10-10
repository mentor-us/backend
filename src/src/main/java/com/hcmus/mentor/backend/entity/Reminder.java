package com.hcmus.mentor.backend.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("reminder")
public class Reminder {
  @Id private String id;
  private ReminderType type;
  private String subject;
  private String name;
  private Date reminderDate;
  private String remindableId;
  private List<String> recipients;
  private String groupId;
  @Builder.Default private Map<String, Object> properties = new HashMap<>();

  public enum ReminderType {
    TASK,
    MEETING
  }
}
