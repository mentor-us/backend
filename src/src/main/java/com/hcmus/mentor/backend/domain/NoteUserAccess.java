package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.NotePermission;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "user_note_access")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(value = {"note", "user"}, allowGetters = true)
public class NoteUserAccess extends BaseDomain implements Serializable {

    @Builder.Default
    @Column(name = "note_permission", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotePermission notePermission = NotePermission.VIEW;

    @JoinColumn(name = "note_id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;
}