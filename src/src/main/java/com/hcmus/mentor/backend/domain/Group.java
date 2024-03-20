package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

@Data
@Builder
@ToString
@Entity
@Table(name = "groups")
//@Document("group")
public class Group implements Serializable {

    /**
     * Map of group status
     */
    private static Map<GroupStatus, String> statusMap;

    /**
     * Group identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
    @OneToMany
    @JoinColumn(name = "mentor_id")
    @Builder.Default
    private List<User> mentors = new ArrayList<>();


    /**
     * List of mentees
     */
    @OneToMany
    @JoinColumn(name = "mentee_id")
    @Builder.Default
    private List<User> mentees = new ArrayList<>();


    /**
     * Group category identifier
     */
    @ManyToOne
    @JoinColumn(name = "group_category_id")
    private GroupCategory groupCategory;


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
//    @Builder.Default
//    private List<String> pinnedMessageIds;

    @OneToMany
    @JoinColumn(name = "pinned_message_ids")
    private List<Message> pinnedMessages;


    /**
     * Default channel identifier
     */
    @OneToOne
    @JoinColumn(name = "default_channel_id")
    private Channel defaultChannel;

    /**
     * List of channel identifiers
     */
    @Builder.Default
    @OneToMany
    @JoinColumn(name = "channel_id")
    private List<Channel> channels = new ArrayList<>();

    /**
     * List of private channel identifiers
     */
    @Builder.Default
    @OneToMany
    @JoinColumn(name = "private_id")
    private List<Channel> privateChannels = new ArrayList<>();

    /**
     * List of FAQ identifiers
     */
    @Builder.Default
    @OneToMany
    @JoinColumn(name = "faq_id")
    private List<Faq> faqIds = new ArrayList<>();

    /**
     * List of marked mentee identifiers
     */
    @Builder.Default
    @OneToMany
    @JoinColumn(name = "marked_mentee_id")
    private List<User> markedMentees = new ArrayList<>();

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
        return mentors.stream().anyMatch(mentor -> mentor.getId().equals(userId));
    }

    public boolean isMentor(User user) {
        return mentees.stream().anyMatch(mentor -> mentor.getId().equals(user.getId()));
    }

    public boolean isMentee(String userId) {
        return mentees.stream().anyMatch(mentee -> mentee.getId().equals(userId));
    }

    public boolean isMentee(User user) {
        return mentees.stream().anyMatch(menteee -> menteee.getId().equals(user.getId()));
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

    public void ping() {
        setUpdatedDate(new Date());
    }
}
