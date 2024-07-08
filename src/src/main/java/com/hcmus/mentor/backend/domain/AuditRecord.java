package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "audit_record")
@JsonIgnoreProperties(value = {"user"}, allowSetters = true)
public class AuditRecord extends BaseDomain {

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "detail", columnDefinition = "TEXT", nullable = false)
    private String detail;

    @BatchSize(size = 10)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}