package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.domain.constant.NotificationType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "notifications")
@Builder
public class Notification {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type = NotificationType.NEW_MESSAGE;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = new Date();

    @Column(name = "ref_id")
    private String refId;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Builder.Default
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "notification", fetch = FetchType.LAZY)
    private List<NotificationUser> receivers = new ArrayList<>();

    public Notification() {

    }
}
