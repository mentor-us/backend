package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.domain.constant.EmojiType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Builder
@Entity
@Table(
        name = "reactions",
        indexes = {@Index(
                name = "idx_reaction",
                columnList = "message_id,emoji_type,user_id",
                unique = true)})
@NoArgsConstructor
@AllArgsConstructor
public class Reaction implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "total")
    private Integer total;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "emoji_type", nullable = false)
    private EmojiType emojiType = EmojiType.LIKE;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnoreProperties(value = {"messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs", "groupUsers", "channels", "tasksAssigner", "tasksAssignee"}, allowSetters = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "message_id", referencedColumnName = "id")
    @JsonIgnoreProperties(value = {"channel", "sender", "reply", "vote", "file", "meeting", "task", "reactions"}, allowSetters = true)
    private Message message;

    public void react() {
        this.total++;
    }

    public void update(User reactor) {
        this.name = reactor.getName();

        String imageUrl = reactor.getImageUrl();
        if (reactor.getImageUrl() != null
                && "https://graph.microsoft.com/v1.0/me/photo/$value".equals(reactor.getImageUrl())) {
            imageUrl = null;
        }
        this.imageUrl = imageUrl;
    }
}
