package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
@Document("group")
public class Group implements Serializable {

    public static final int MAX_PINNED_MESSAGES = 5;
    private static Map<GroupStatus, String> statusMap;
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
    private GroupStatus status = GroupStatus.ACTIVE;
    private Date timeStart;
    private Date timeEnd;
    private Duration duration;
    private String imageUrl;
    private String creatorId;
    private Boolean hasNewMessage;
    @Builder.Default
    private List<String> pinnedMessageIds = new ArrayList<>();
    @Builder.Default
    private String parentId = null;
    @Builder.Default
    private List<String> channelIds = new ArrayList<>();
    @Builder.Default
    private List<String> privateIds = new ArrayList<>();
    @Builder.Default
    private List<String> faqIds = new ArrayList<>();
    @Builder.Default
    private String lastMessage = null;
    @Builder.Default
    private String lastMessageId = null;
    @Builder.Default
    private List<String> markedMenteeIds = new ArrayList<>();

    public Group() {
        statusMap = new EnumMap<>(GroupStatus.class);
        initializeStatusMap();
    }

    public static Map<GroupStatus, String> getStatusMap() {
        return statusMap;
    }

    private void initializeStatusMap() {
        statusMap.put(GroupStatus.ACTIVE, "Đang hoạt động");
        statusMap.put(GroupStatus.DISABLED, "Bị khoá");
        statusMap.put(GroupStatus.OUTDATED, "Hết thời hạn");
        statusMap.put(GroupStatus.INACTIVE, "Chưa hoạt động");
        statusMap.put(GroupStatus.DELETED, "Đã xóa");
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

    public void update(
            String name,
            String description,
            GroupStatus status,
            Date timeStart,
            Date timeEnd,
            String groupCategory) {
        if (name != null) {
            this.setName(name);
        }
        if (description != null) {
            this.setDescription(description);
        }
        if (status != null) {
            this.setStatus(status);
        }
        if (timeStart != null) {
            timeStart.setHours(0);
            timeStart.setMinutes(0);
            this.setTimeStart(timeStart);
        }
        if (timeEnd != null) {
            timeStart.setHours(23);
            timeStart.setMinutes(59);
            this.setTimeEnd(timeEnd);
        }
        if (groupCategory != null) {
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

        if (channelIds == null) {
            setChannelIds(new ArrayList<>());
        }

        if (privateIds == null) {
            setPrivateIds(new ArrayList<>());
        }

        if (markedMenteeIds == null) {
            markedMenteeIds = new ArrayList<>();
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
        normalize();

        if (channelIds.contains(channelId)) {
            return;
        }
        channelIds.add(channelId);
        setChannelIds(channelIds);
    }

    public void addPrivate(String channelId) {
        normalize();

        if (privateIds.contains(channelId)) {
            return;
        }
        privateIds.add(channelId);
        setPrivateIds(privateIds);
    }

    public void removeChannel(String channelId) {
        normalize();

        channelIds.remove(channelId);
        privateIds.remove(channelId);
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
        return Arrays.asList(GroupStatus.DISABLED, GroupStatus.INACTIVE, GroupStatus.DELETED).contains(status);
    }

    public List<String> getMembers() {
        return Stream.concat(mentees.stream(), mentors.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public void markMentee(String menteeId) {
        normalize();

        if (markedMenteeIds == null) {
            markedMenteeIds = new ArrayList<>();
        }

        if (!mentees.contains(menteeId)) {
            return;
        }

        if (markedMenteeIds.contains(menteeId)) {
            return;
        }

        markedMenteeIds.add(menteeId);
    }

    public void unmarkMentee(String menteeId) {
        normalize();

        if (markedMenteeIds == null) {
            markedMenteeIds = new ArrayList<>();
        }

        if (!mentees.contains(menteeId)) {
            return;
        }

        if (!markedMenteeIds.contains(menteeId)) {
            return;
        }

        markedMenteeIds.remove(menteeId);
    }

}
