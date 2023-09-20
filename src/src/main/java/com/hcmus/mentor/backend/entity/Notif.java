package com.hcmus.mentor.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("notification")
public class Notif {

    @Id
    private String id;

    private String title;

    private String content;

    private Type type;

    private String senderId;

    private String refId;
    
    @Builder.Default
    private List<String> receiverIds = new ArrayList<>();

    @Builder.Default
    private Date createdDate = new Date();

    @Builder.Default
    private List<String> readers = new ArrayList<>();

    @Builder.Default
    private List<String> agrees = new ArrayList<>();

    @Builder.Default
    private List<String> refusers = new ArrayList<>();

    public enum Type {
        SYSTEM,
        NEW_COMMENT,
        NEW_MESSAGE,
        NEW_TASK,
        NEW_MEETING,
        NEW_REACTION,
        NEW_IMAGE_MESSAGE,
        NEW_FILE_MESSAGE,
        UPDATE_MEETING,
        RESCHEDULE_MEETING,
        NEW_VOTE,
        PIN_MESSAGE,
        UNPIN_MESSAGE,
        NEED_RESPONSE
    }

    public void seen(String userId) {
        if (readers.contains(userId)) {
            return;
        }
        readers.add(userId);
    }

    public void accept(String userId) {
        seen(userId);
        if (agrees.contains(userId)) {
            return;
        }
        agrees.add(userId);
    }

    public void refuse(String userId) {
        seen(userId);
        if (refusers.contains(userId)) {
            return;
        }
        refusers.add(userId);
    }
}
