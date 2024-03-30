package com.hcmus.mentor.backend.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@Table(name = "group_user")
public class GroupUser {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Builder.Default
    @Column(name = "is_mentor")
    private boolean isMentor = false;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public GroupUser() {

    }
}
