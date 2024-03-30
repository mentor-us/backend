package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
@Document("group")
public class Group implements Serializable {

    /**
     * Maximum number of pinned messages in a group
     */
    public static final int MAX_PINNED_MESSAGES = 5;

    /**
     * Map of group status
     */
    private static Map<GroupStatus, String> statusMap;

    /**
     * Group identifier
     */
    @Id
    private String id;

    /**
     * Group name
     */
    private String name;

    /**
     * Group description
     */
    private String description;

    /**
     * List of mentors
     */
    @Builder.Default
    private List<String> mentors = new ArrayList<>();

    /**
     * List of mentees
     */
    @Builder.Default
    private List<String> mentees = new ArrayList<>();

    /**
     * Group category identifier
     */
    private String groupCategory;

    /**
     * Creator identifier
     */
    private String creatorId;

    /**
     * Status of the group
     */
    private GroupStatus status = GroupStatus.ACTIVE;

    /**
     * Avatar image URL
     */
    private String imageUrl;

    /**
     * Parent group identifier
     */
    @Builder.Default
    private String parentId = null;

    /**
     * Flag to indicate if the group has new message
     */
    private Boolean hasNewMessage;

    /**
     * Last message
     */
    @Builder.Default
    private String lastMessage = null;

    /**
     * Last message identifier
     */
    @Builder.Default
    private String lastMessageId = null;

    /**
     * List of pinned message identifiers
     */
    @Builder.Default
    private List<String> pinnedMessageIds = new ArrayList<>();

    /**
     * Default channel identifier
     */
    private String defaultChannelId;

    /**
     * List of channel identifiers
     */
    @Builder.Default
    private List<String> channelIds = new ArrayList<>();

    /**
     * List of private channel identifiers
     */
    @Builder.Default
    private List<String> privateIds = new ArrayList<>();

    /**
     * List of FAQ identifiers
     */
    @Builder.Default
    private List<String> faqIds = new ArrayList<>();

    /**
     * List of marked mentee identifiers
     */
    @Builder.Default
    private List<String> markedMenteeIds = new ArrayList<>();

    /**
     * Time start of the group
     */
    private Date timeStart;

    /**
     * Time end of the group
     */
    private Date timeEnd;

    /**
     * Duration of the group
     */
    private Duration duration;

    /**
     * Created date
     */
    @Builder.Default
    private Date createdDate = new Date();
    /**
     * Updated date
     */
    @Builder.Default
    private Date updatedDate = new Date();

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
                .toList();
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
