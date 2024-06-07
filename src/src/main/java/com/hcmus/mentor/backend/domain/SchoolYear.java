package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "school_years")
@JsonIgnoreProperties(value = {"semesters"}, allowSetters = true)
public class SchoolYear extends BaseDomain {

    @Column(name = "name", nullable = false)
    private String name;

    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "year", fetch = FetchType.LAZY)
    private List<Semester> semesters = new ArrayList<>();
}