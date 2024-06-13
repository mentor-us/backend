package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "courses")
@JsonIgnoreProperties(value = {"grades"}, allowSetters = true)
public class Course extends BaseDomain implements Serializable {

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private Set<Grade> grades = new HashSet<>();
}