package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.FileModel;
import com.hcmus.mentor.backend.controller.payload.request.EditMessageRequest;
import com.hcmus.mentor.backend.domain.constant.EmojiType;
import com.hcmus.mentor.backend.domain.dto.ReactionDto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("group_message")
@ToString
public class Message {

    @Id
    private String id;

    private String senderId;

    @TextIndexed(weight = 3)
    private String content;

    private Date createdDate;

    private Type type = Type.TEXT;

    private String groupId;

    private String voteId;

    private String meetingId;

    private String taskId;

    private Boolean isEdited = false;

    private Date editedAt = null;

    @Builder.Default
    private List<ReactionDto> reactions = new ArrayList<>();

    @Builder.Default
    private List<String> images = new ArrayList<>();

    private FileModel file;

    @Builder.Default
    private Status status = Status.SENT;

    private SystemLog systemLog;

    private String reply;

    private Boolean isForward = false;

    public ReactionDto react(String userId, EmojiType emoji) {
        Optional<ReactionDto> reactionWrapper =
                reactions.stream().filter(r -> r.getUserId().equals(userId)).findFirst();
        if (!reactionWrapper.isPresent()) {
            ReactionDto newReaction = ReactionDto.builder().userId(userId).total(0).build();
            newReaction.react(emoji);
            reactions.add(newReaction);
            return newReaction;
        }
        ReactionDto reaction = reactionWrapper.get();
        reaction.react(emoji);
        setReactions(reactions);
        return reaction;
    }

    public void removeReact(String userId) {
        List<ReactionDto> filteredReactions =
                reactions.stream()
                        .filter(reaction -> !reaction.getUserId().equals(userId))
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
