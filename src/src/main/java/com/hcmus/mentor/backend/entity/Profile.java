package com.hcmus.mentor.backend.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("user_profile")
public class Profile {

  @Id private String id;

  private String studentId;

  private String schoolYear;

  private String major;

  private String className;

  private String description;

  private String phone;

  private String email;

  private String facebook;

  @Indexed(unique = true)
  private String userId;
}
