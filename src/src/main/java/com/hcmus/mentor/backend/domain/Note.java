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
@Table(name = "notes")
@JsonIgnoreProperties(value = {"creator", "owner", "users", "noteHistories", "updatedBy", "noteUserAccesses"}, allowSetters = true)
public class Note extends BaseDomain implements Serializable {

    @NonNull
    @Column(name = "title", nullable = false)
    private String title;

    @NonNull
    @Column(name = "content", nullable = false)
    private String content;

    @Builder.Default
    @Column(name = "is_public")
    private Boolean isPublic = false;

    @NonNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id")
    private User creator;

    @NonNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "note", fetch = FetchType.LAZY)
    private Set<NoteHistory> noteHistories = new HashSet<>();

    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "note", fetch = FetchType.LAZY)
    private Set<NoteUserAccess> noteUserAccesses = new HashSet<>();

    @Builder.Default
    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ref_user_note",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"note_id", "user_id"})})
    private Set<User> users = new HashSet<>();
}