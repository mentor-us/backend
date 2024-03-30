package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.File;
import com.hcmus.mentor.backend.controller.payload.request.EditMessageRequest;
import com.hcmus.mentor.backend.domain.constant.EmojiType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "messages")
public class Message {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "content")
    private String content;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = new Date();

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

//    private SystemLog systemLog;

    @Column(name = "reply")
    private String reply;

    @Builder.Default
    @Column(name = "is_forward", nullable = false)
    private Boolean isForward = false;

    @Builder.Default
    @ElementCollection
    private List<String> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Builder.Default
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id")
    private Vote vote = null;

    @Builder.Default
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file = null;

    @Builder.Default
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting = null;

    @Builder.Default
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task = null;

    @Builder.Default
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reaction> reactions = new ArrayList<>();


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

    public void removeReact(User user) {
        List<Reaction> filteredReactions =
                reactions.stream()
                        .filter(reaction -> !reaction.getUser().equals(user))
                        .toList();
        setReactions(filteredReactions);
    }

    public void edit(EditMessageRequest request) {
        setContent(request.getNewContent());
        setStatus(Status.EDITED);
        setEditedAt(new Date());
        setIsEdited(true);
    }

    public void delete() {
        setStatus(Status.DELETED);
    }

    public boolean isDeleted() {
        return Status.DELETED.equals(status);
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
