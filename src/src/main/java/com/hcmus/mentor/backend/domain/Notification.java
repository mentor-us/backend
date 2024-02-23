package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.domain.constant.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("notification")
public class Notification {

    @Id
    private String id;

    private String title;

    private String content;

    private NotificationType type;

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
