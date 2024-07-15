package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "choices")
@JsonIgnoreProperties(value = {"creator", "vote"}, allowSetters = true)
public class Choice extends BaseDomain {

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id")
    private Vote vote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ref_choice_user",
            joinColumns = @JoinColumn(name = "choice_id"),
            inverseJoinColumns = @JoinColumn(name = "voter_id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"choice_id", "voter_id"})})
    private List<User> voters = new ArrayList<>();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "vote = " + vote + ", " +
                "creator = " + creator + ")";
    }
}