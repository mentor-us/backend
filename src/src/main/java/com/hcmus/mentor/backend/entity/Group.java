package com.hcmus.mentor.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
@Document("group")
public class Group implements Serializable {

    public static final int MAX_PINNED_MESSAGES = 5;

    @Id
    private String id;

    private String name;

    private String description;

    @Builder.Default
    private Date createdDate = new Date();

    @Builder.Default
    private Date updatedDate = new Date();

    @Builder.Default
    private List<String> mentors = new ArrayList<>();

    @Builder.Default
    private List<String> mentees = new ArrayList<>();

    private String groupCategory;

    @Builder.Default
    private Status status = Status.ACTIVE;

    private Date timeStart;

    private Date timeEnd;

    private Duration duration;

    private String imageUrl;

    private String creatorId;

    private Boolean hasNewMessage;

    @Builder.Default
    private List<String> pinnedMessageIds = new ArrayList<>();

    @Builder.Default
    private Type type = Type.NORMAL;

    @Builder.Default
    private String parentId = null;

    @Builder.Default
    private List<String> channels = new ArrayList<>();

    @Builder.Default
    private List<String> privates = new ArrayList<>();

    @Builder.Default
    private List<String> faqIds = new ArrayList<>();

    @Builder.Default
    private String lastMessage = null;

    public enum Type {
        NORMAL,
        CHANNEL,
        PRIVATE,
    }

    public enum Status {
        ACTIVE,
        DISABLED,
        OUTDATED,
        INACTIVE,
        DELETED
    }

    private static Map<Status, String> statusMap;

    public Group() {
        statusMap = new EnumMap<>(Status.class);
        initializeStatusMap();
    }

    private void initializeStatusMap() {
        statusMap.put(Status.ACTIVE, "Đang hoạt động");
        statusMap.put(Status.DISABLED, "Bị khoá");
        statusMap.put(Status.OUTDATED, "Hết thời hạn");
        statusMap.put(Status.INACTIVE, "Chưa hoạt động");
        statusMap.put(Status.DELETED, "Đã xóa");
    }

    public static Map<Status, String> getStatusMap() {
        return statusMap;
    }

    public boolean isMentor(String userId) {
        return mentors.contains(userId);
    }

    public boolean isMentee(String userId) {
        return mentees.contains(userId);
    }

    public void addMentee(String menteeId) {
        mentees.add(menteeId);
    }

    public void addMentor(String mentorId) {
        mentors.add(mentorId);
    }

    public boolean isMember(String userId) {
        return isMentee(userId) || isMentor(userId);
    }

    public void update(String name, String description, Status status, Date timeStart, Date timeEnd, String groupCategory) {
        if(name != null){
            this.setName(name);
        }
        if(description != null){
            this.setDescription(description);
        }
        if(status != null) {
            this.setStatus(status);
        }
        if(timeStart != null) {
            timeStart.setHours(0);
            timeStart.setMinutes(0);
            this.setTimeStart(timeStart);
        }
        if(timeEnd != null) {
            timeStart.setHours(23);
            timeStart.setMinutes(59);
            this.setTimeEnd(timeEnd);
        }
        if(groupCategory != null) {
            this.setGroupCategory(groupCategory);
        }
    }

    public Integer getTotalMember() {
        return getMentees().size() + getMentors().size();
    }

    public void ping() {
        setUpdatedDate(new Date());
    }

    public void normalize() {
        if (pinnedMessageIds == null) {
            setPinnedMessageIds(new ArrayList<>());
        }

        if (faqIds == null) {
            setFaqIds(new ArrayList<>());
        }

        if (channels == null) {
            setChannels(new ArrayList<>());
        }

        if (privates == null) {
            setPrivates(new ArrayList<>());
        }
    }
    public void pinMessage(String messageId) {
        normalize();

        if (isMaximumPinnedMessages()) {
            return;
        }
        if (pinnedMessageIds.contains(messageId)) {
            return;
        }
        pinnedMessageIds.add(messageId);
    }

    public void unpinMessage(String messageId) {
        normalize();

        if (!pinnedMessageIds.contains(messageId)) {
            return;
        }
        pinnedMessageIds.remove(messageId);
    }

    public boolean isMaximumPinnedMessages() {
        normalize();

        return pinnedMessageIds.size() >= MAX_PINNED_MESSAGES;
    }

    public void addChannel(String channelId) {
        channels.add(channelId);
        setChannels(channels);
    }

    public void addFaq(String faqId) {
        normalize();

        if (faqIds.contains(faqId)) {
            return;
        }
        faqIds.add(faqId);
    }

    public void importFaq(List<String> faqIds) {
        normalize();

        faqIds.forEach(this::addFaq);
    }

    public void deleteFaq(String faqId) {
        normalize();

        if (!faqIds.contains(faqId)) {
            return;
        }
        faqIds.remove(faqId);
    }

    public boolean isStopWorking() {
        return Arrays.asList(Status.DISABLED, Status.INACTIVE, Status.DELETED)
                .contains(status);
    }
}
