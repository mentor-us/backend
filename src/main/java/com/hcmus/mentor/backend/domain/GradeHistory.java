package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "grade_histories")
@JsonIgnoreProperties(value = {"gradeVersion"}, allowSetters = true)
public class GradeHistory extends BaseDomain {

    @Column(name = "score")
    private Double score;

    @Column(name = "value")
    private String value;

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "year")
    private String year;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "course_code")
    private String courseCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_version_id")
    @ToString.Exclude
    private GradeVersion gradeVersion;
}