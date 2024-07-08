package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.NotePermission;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "note_user_access",
        indexes = {@Index(
                name = "idx_note_user_access_unit",
                columnList = "note_id,user_id",
                unique = true
        )})
@JsonIgnoreProperties(value = {"note", "user"}, allowSetters = true)
public class NoteUserAccess extends BaseDomain {

    @Builder.Default
    @Column(name = "note_permission", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotePermission notePermission = NotePermission.VIEW;

    @Nonnull
    @JoinColumn(name = "note_id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    private Note note;

    @Nonnull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;
}