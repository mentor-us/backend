package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "semesters")
@JsonIgnoreProperties(value = {"grades"}, allowSetters = true)
public class Semester extends BaseDomain implements Serializable {

    @Column(name = "name", nullable = false)
    private String name;

    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "semester", fetch = FetchType.LAZY)
    private List<Grade> grades = new ArrayList<>();
}