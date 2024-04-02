package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.domain.constant.EmojiType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@Entity
@Table(
        name = "reactions",
        indexes = {@Index(
                name = "idx_reaction",
                columnList = "message_id,emoji_type,user_id",
                unique = true)})
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", referencedColumnName = "id")
    private Message message;

    public Reaction() {

    }

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
