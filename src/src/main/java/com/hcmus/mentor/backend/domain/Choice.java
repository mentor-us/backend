package com.hcmus.mentor.backend.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@Table(name = "choices")
public class Choice {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id")
    private Vote vote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Builder.Default
    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ref_choice_user",
        joinColumns = @JoinColumn(name = "choice_id"),
        inverseJoinColumns = @JoinColumn(name = "voter_id")
    )
    private List<User> voters = new ArrayList<>();

    public Choice() {

    }
}
