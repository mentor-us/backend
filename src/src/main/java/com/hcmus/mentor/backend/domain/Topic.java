package com.hcmus.mentor.backend.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
@Document("topic")
public class Topic {

    @Id
    private String id;

    private String name;

    private String description;

    private Date createdDate;
}
