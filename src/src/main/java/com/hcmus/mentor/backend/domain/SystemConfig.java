package com.hcmus.mentor.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "system-config")
public class SystemConfig {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "type")
    private String type;

    @Column(name = "key")
    private String key;

    @Column(name = "value", columnDefinition = "VARCHAR(255)")
    private Object value;

}