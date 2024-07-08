package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "grade_user_access",
        indexes = {@Index(name = "idx_user_user_access_unit", columnList = "user_id,user_access_id", unique = true)})
@JsonIgnoreProperties(value = {"user", "userAccess"}, allowSetters = true)
public class GradeUserAccess extends BaseDomain {

    @NonNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NonNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_access_id", nullable = false)
    private User userAccess;
}