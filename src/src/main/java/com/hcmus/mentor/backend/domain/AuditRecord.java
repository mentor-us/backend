package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

@Entity
@Getter
@Setter
@Table(name = "audit_record")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(value = {"user"}, allowSetters = true)
public class AuditRecord extends BaseDomain {

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ActionType action ;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain", nullable = false)
    private DomainType domain;

    @Column(name = "detail", columnDefinition = "TEXT", nullable = false)
    private String detail;

    @BatchSize(size = 10)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}