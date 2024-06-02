package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.controller.payload.request.EditMessageRequest;
import com.hcmus.mentor.backend.domain.constant.EmojiType;
import com.hcmus.mentor.backend.util.DateUtils;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "messages")
@JsonIgnoreProperties(value = {"channel", "sender", "reply", "vote", "file", "meeting", "task", "reactions"}, allowSetters = true)
public class Message implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "content")
    private String content;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = DateUtils.getCurrentDateAtUTC();

    @Builder.Default
    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;

    @Builder.Default
    @Column(name = "edited_at")
    private Date editedAt = null;

    @Builder.Default
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type = Type.TEXT;

    @Builder.Default
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.SENT;

    @Column(name = "reply")
    private String reply;

    @Builder.Default
    @Column(name = "is_forward", nullable = false)
    private Boolean isForward = false;

    @Builder.Default
    @ElementCollection
    private List<String> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @BatchSize(size = 10)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Builder.Default
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "vote_id")
    private Vote vote = null;

    @Builder.Default
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "file_id")
    private File file = null;

    @Builder.Default
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting = null;

    @Builder.Default
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "task_id")
    private Task task = null;

    @Builder.Default
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<Reaction> reactions = new ArrayList<>();

    @PrePersist
    public void ensureId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public Reaction react(User user, EmojiType emoji) {
        var reaction = reactions.stream().filter(r -> r.getUser().equals(user) && r.getEmojiType().equals(emoji)).findFirst().orElse(null);
        if (reaction == null) {
            Reaction newReaction = Reaction.builder().user(user).message(this).emojiType(emoji).total(0).build();
            newReaction.react();
            reactions.add(newReaction);
            return newReaction;
        }

        reaction.react();
        setReactions(reactions);
        return reaction;
    }

    public void edit(EditMessageRequest request) {
        setContent(request.getNewContent());
        setStatus(Status.EDITED);
        setEditedAt(DateUtils.getCurrentDateAtUTC());
        setIsEdited(true);
    }

    public void delete() {
        setStatus(Status.DELETED);
    }

    public boolean isDeleted() {
        return Status.DELETED.equals(status);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "images = " + images + ", " +
                "isForward = " + isForward + ", " +
                "reply = " + reply + ", " +
                "status = " + status + ", " +
                "type = " + type + ", " +
                "editedAt = " + editedAt + ", " +
                "isEdited = " + isEdited + ", " +
                "createdDate = " + createdDate + ", " +
                "content = " + content + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Message other)) return false;
        if (!this.id.equals(other.id)) return false;
        return true;
    }

    public enum Type {
        TEXT,
        FILE,
        IMAGE,
        VIDEO,
        MEETING,
        TASK,
        VOTE,
        NOTIFICATION,
        SYSTEM
    }

    public enum Status {
        SENT,
        EDITED,
        DELETED
    }

    public enum SystemIcon {
        PIN,
        UNPIN,
        MEETING,
        TASK,
        VOTING,
        MEMBER
    }

    public enum SystemLogType {
        NEW_TASK,
        NEW_MEETING,
        UPDATE_MEETING,
        RESCHEDULE_MEETING,
        NEW_VOTE,
        CLOSED_VOTE,
        PIN_MESSAGE,
        UNPIN_MESSAGE
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Setter
    public static class SystemLog {
        private SystemIcon icon;

        private String refId;

        private SystemLogType type;
    }
}