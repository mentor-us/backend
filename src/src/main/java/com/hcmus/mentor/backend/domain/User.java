package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.request.UpdateStudentInformationRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateUserForAdminRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateUserRequest;
import com.hcmus.mentor.backend.domain.constant.AuthProvider;
import com.hcmus.mentor.backend.domain.constant.UserGender;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import com.hcmus.mentor.backend.security.principal.oauth2.CustomerOidcUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

import static com.hcmus.mentor.backend.domain.constant.UserRole.USER;

@Data
@Builder
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Builder.Default
    private String name = "";

    @Email
    private String email;

    @Builder.Default
    @ElementCollection
    private List<String> additionalEmails = new ArrayList<>();

    @Builder.Default
    private String imageUrl = "";

    @Builder.Default
    private String wallpaper = "";

    @Builder.Default
    private Boolean emailVerified = true;

    @Builder.Default
    private String password = "";

    @NotNull
    @Builder.Default
    private AuthProvider provider = AuthProvider.local;

    @Builder.Default
    private String providerId = "";

    @Builder.Default
    private boolean status = true;

    @Builder.Default
    private String phone = "";

    private Date birthDate;

    @Builder.Default
    private Date createdDate = new Date();

    private int trainingPoint;

    private Boolean hasEnglishCert;

    private double studyingPoint;

    private String initialName;

    @Builder.Default
    private UserGender gender = UserGender.MALE;

    @Builder.Default
    @ElementCollection
    private List<UserRole> roles = new ArrayList<>(List.of(USER));




    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Reaction> reactions = new ArrayList<>();




    @Builder.Default
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();



    // === Vote ===
    @Builder.Default
    @OneToMany(mappedBy = "creator")
    private List<Vote> votesCreated = new ArrayList<>();


    // === Choice ===
    @Builder.Default
    @OneToMany(mappedBy = "creator")
    private List<Choice> choicesCreated = new ArrayList<>();


    @Builder.Default
    @ManyToMany(mappedBy = "voters")
    private List<Choice> choices = new ArrayList<>();



    // === Meeting ===
    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<MeetingHistory> meetingHistories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "organizer")
    private List<Meeting> meetingsOrganizer = new ArrayList<>();

    @Builder.Default
    @ManyToMany(mappedBy = "attendees")
    private List<Meeting> meetingAttendees = new ArrayList<>();


    // === Notification ===
    @Builder.Default
    @OneToMany(mappedBy = "sender")
    private List<Notification> notificationsSent = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<NotificationUser> notifications = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<NotificationSubscriber> notificationSubscribers = new ArrayList<>();

    // === Reaction ===

    // === Reminder ===
    @Builder.Default
    @ManyToMany(mappedBy = "recipients")
    private List<Reminder> reminders = new ArrayList<>();


    // === Faq ===
    @Builder.Default
    @OneToMany(mappedBy = "creator")
    private List<Faq> faqsCreated = new ArrayList<>();

    @Builder.Default
    @ManyToMany(mappedBy = "voters")
    private List<Faq> faqs = new ArrayList<>();


    // === Group ===
    @Builder.Default
    @OneToMany(mappedBy = "creator")
    private List<Group> groupsCreated = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<GroupUser> groupUsers = new ArrayList<>();

    @Builder.Default
    @ManyToMany(mappedBy = "menteesMarked")
    private List<Group> groupsMenteeMarked = new ArrayList<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "ref_user_group_pinned",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private List<Group> groupsPinned = new ArrayList<>();


    // === Channel ===
    @OneToMany(mappedBy = "creator")
    private List<Channel> channelsCreated;

    @ManyToMany(mappedBy = "users")
    private List<Channel> channels;


    // === Task ===
    @Builder.Default
    @OneToMany(mappedBy = "assigner")
    private List<Task> tasksAssigner = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Assignee> tasksAssignee = new ArrayList<>();


    public User() {

    }

    public boolean isPinnedGroup(String groupId) {
        return groupsPinned.stream().anyMatch(group -> group.getId().equals(groupId));
    }

    public void pinGroup(Group group) {
        if (isPinnedGroup(group.getId())) {
            return;
        }
        groupsPinned.add(group);
    }

    public void unpinGroup(String groupId) {
        if (!isPinnedGroup(groupId)) {
            return;
        }
        groupsPinned.add(groupsPinned.stream().filter(group -> group.getId().equals(groupId)).findFirst().get());
    }

    @Override
    public String toString() {
        return "Người dùng: "
                + "id='"
                + id
                + '\''
                + ", Tên='"
                + name
                + '\''
                + ", email='"
                + email
                + '\'';
    }

    public void update(CustomerOidcUser customerOidcUser) {
        if (name == null || name.equals("") || name.equals(initialName)) {
            name = customerOidcUser.getName();
        }
        if (!("https://graph.microsoft.com/v1.0/me/photo/$value").equals(customerOidcUser.getImageUrl())) {
            imageUrl = (imageUrl == null || imageUrl.equals("")) ? customerOidcUser.getImageUrl() : imageUrl;
        }
    }

    public void update(UpdateUserRequest request) {
        name = request.getName();
        imageUrl = request.getImageUrl();
        phone = request.getPhone();
        birthDate = request.getBirthDate();
        gender = request.getGender();
    }

    public void update(UpdateUserForAdminRequest request) {
        name = request.getName();
        phone = request.getPhone();
        status = request.isStatus();
        birthDate = request.getBirthDate();
        gender = request.getGender();
    }

    public void update(UpdateStudentInformationRequest request) {
        if (request.getTrainingPoint() != null) {
            trainingPoint = request.getTrainingPoint();
        }
        if (request.getHasEnglishCert() != null) {
            hasEnglishCert = request.getHasEnglishCert();
        }
        if (request.getStudyingPoint() != null) {
            studyingPoint = request.getStudyingPoint();
        }
    }

    public void activate() {
        this.setEmailVerified(true);
    }

    public void updateAvatar(String url) {
        if (url.isEmpty()) {
            return;
        }
        setImageUrl(url);
    }

    public void updateWallpaper(String url) {
        if (url.isEmpty()) {
            return;
        }
        setWallpaper(url);
    }

    public void assignRole(UserRole role) {
        if (roles.contains(role)) {
            return;
        }
        roles.add(role);
        setRoles(roles);
    }

}
