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
@Table(name = "grades",
        indexes = {
                @Index(name = "idx_grades_info", columnList = "year,semester,course_code,course_name"),
                @Index(name = "idx_grades_student", columnList = "student_id")})
@JsonIgnoreProperties(value = {"creator", "student"}, allowSetters = true)
public class Grade extends BaseDomain {

    @Builder.Default
    @Column(name = "score", nullable = false)
    private Double score = 0.0;

    @Builder.Default
    @Column(name = "is_retake", nullable = false)
    private Boolean isRetake = false;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "year")
    private String year;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "course_code")
    private String courseCode;
}