package com.hcmus.mentor.backend.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("system-config")
public class SystemConfig {
    @Id
    private String id;

    private String name;

    private String description;

    private String type;
    private String key;

    private Object value;
}
