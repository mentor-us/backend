package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "grade_versions")
@JsonIgnoreProperties(value = {"creator", "user"}, allowSetters = true)
public class GradeVersion extends BaseDomain {

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    @ToString.Exclude
    private User creator;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Builder.Default
    @ToString.Exclude
    @Fetch(FetchMode.JOIN)
    @OneToMany(mappedBy = "gradeVersion", fetch = FetchType.LAZY)
    private List<Grade> grades = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @Fetch(FetchMode.JOIN)
    @OneToMany(mappedBy = "gradeVersion", fetch = FetchType.LAZY)
    private List<GradeHistory> histories = new ArrayList<>();
}