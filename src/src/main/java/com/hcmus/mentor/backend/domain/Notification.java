package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.domain.constant.NotificationType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "notifications")
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;

    private String content;

    private NotificationType type;

    private String senderId;

    private String refId;

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "receiver_id")
    private List<User> receivers = new ArrayList<>();

    @Builder.Default
    private Date createdDate = new Date();

    @Builder.Default
    @OneToMany()
    @JoinColumn(name = "reader_id")
    private List<User> readers = new ArrayList<>();

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "agrees_id")
    private List<User> agrees = new ArrayList<>();

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "refusers_id")
    private List<User> refusers = new ArrayList<>();

    public Notification() {

    }
}
