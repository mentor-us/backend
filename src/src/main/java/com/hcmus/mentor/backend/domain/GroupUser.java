package com.hcmus.mentor.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@Table(name = "group_user")
@NoArgsConstructor
@AllArgsConstructor
public class GroupUser {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Builder.Default
    @Column(name = "is_mentor")
    private boolean isMentor = false;

    @Builder.Default
    @Column(name = "is_pinned")
    private boolean isPinned = false;

    @Builder.Default
    @Column(name = "is_marked")
    private boolean isMarked = false;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
