package com.hcmus.mentor.backend.payload.response.configuration;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfigurationDetailResponse {

  @Id
  private String id;
  private String name;
  private String value;
  private Date updatedAt;
  private String updatedBy;
}
