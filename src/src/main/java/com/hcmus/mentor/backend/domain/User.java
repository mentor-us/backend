package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.hcmus.mentor.backend.domain.constant.UserRole.USER;

@Setter
@Getter
@Entity
@Builder
@ToString
@Table(name = "users")
@AllArgsConstructor
public class User implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Builder.Default
    @ElementCollection
    @Column(name = "additional_emails")
    @JoinTable(name = "user_additional_emails", joinColumns = @JoinColumn(name = "user_id"))
    private List<String> additionalEmails = new ArrayList<>();

    @Builder.Default
    @Column(name = "image_url")
    private String imageUrl = "";

    @Builder.Default
    @Column(name = "wallpaper")
    private String wallpaper = "";

    @Builder.Default
    @Column(name = "email_verified")
    private Boolean emailVerified = true;

    @Builder.Default
    @Column(name = "password")
    private String password = "";

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private AuthProvider provider = AuthProvider.local;

    @Builder.Default
    @Column(name = "provider_id")
    private String providerId = "";

    @Builder.Default
    @Column(name = "status")
    private boolean status = true;

    @Builder.Default
    @Column(name = "phone")
    private String phone = "";

    @Column(name = "birth_date")
    private Date birthDate;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = new Date();

    @Column(name = "training_point")
    private int trainingPoint;

    @Column(name = "has_english_cert")
    private Boolean hasEnglishCert;

    @Column(name = "studying_point")
    private double studyingPoint;

    @Column(name = "initial_name")
    private String initialName;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private UserGender gender = UserGender.MALE;

    @Builder.Default
    @ElementCollection
    private List<UserRole> roles = new ArrayList<>(List.of(USER));


    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Reaction> reactions = new ArrayList<>();


    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();


    // === Vote ===
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<Vote> votesCreated = new ArrayList<>();


    // === Choice ===
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<Choice> choicesCreated = new ArrayList<>();


    @JsonIgnore
    @Builder.Default
    @ManyToMany(mappedBy = "voters", fetch = FetchType.LAZY)
    private List<Choice> choices = new ArrayList<>();


    // === Meeting ===
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "modifier", fetch = FetchType.LAZY)
    private List<MeetingHistory> meetingHistories = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "organizer", fetch = FetchType.LAZY)
    private List<Meeting> meetingsOrganizer = new ArrayList<>();

    @Builder.Default
    @ManyToMany(mappedBy = "attendees", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Meeting> meetingAttendees = new ArrayList<>();


    // === Notification ===
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
    private List<Notification> notificationsSent = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<NotificationUser> notifications = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<NotificationSubscriber> notificationSubscribers = new ArrayList<>();

    // === Reaction ===

    // === Reminder ===
    @Builder.Default
    @ManyToMany(mappedBy = "recipients", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Reminder> reminders = new ArrayList<>();


    // === Faq ===
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<Faq> faqsCreated = new ArrayList<>();

    @Builder.Default
    @ManyToMany(mappedBy = "voters", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Faq> faqs = new ArrayList<>();


    // === Group ===
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<Group> groupsCreated = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<GroupUser> groupUsers = new ArrayList<>();

    // === Channel ===
    @JsonIgnore
    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<Channel> channelsCreated;

    @JsonIgnore
    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Channel> channels;


    // === Task ===
    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "assigner", fetch = FetchType.LAZY)
    private List<Task> tasksAssigner = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Assignee> tasksAssignee = new ArrayList<>();


    public User() {
    }

    public boolean isPinnedGroup(String groupId) {
        return groupUsers.stream().anyMatch(groupUser -> groupUser.getGroup().getId().equals(groupId) && groupUser.isPinned());
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