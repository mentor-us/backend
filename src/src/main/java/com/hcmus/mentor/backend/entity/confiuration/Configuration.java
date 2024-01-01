package com.hcmus.mentor.backend.entity.confiuration;

import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document("configuration")
public class Configuration {

  @Id
  private String id;

  private String name;

  private String value;

  private Date updatedAt;

  private String updatedBy;
}
