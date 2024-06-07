package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "grades")
@JsonIgnoreProperties(value = {"semester", "student"}, allowSetters = true)
public class Grade extends BaseDomain {

    @Column(name = "name", nullable = false)
    private String name;

    @Builder.Default
    @Column(name = "score", nullable = false)
    private Double score = 0.0;

    @Builder.Default
    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id")
    private Semester semester;

}