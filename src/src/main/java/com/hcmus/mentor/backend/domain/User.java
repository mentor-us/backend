package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.controller.payload.request.UpdateStudentInformationRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateUserForAdminRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateUserRequest;
import com.hcmus.mentor.backend.domain.constant.AuthProvider;
import com.hcmus.mentor.backend.domain.constant.UserGender;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseDomain implements Serializable {

//    @Id
//    @Column(name = "id")
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
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

//    @Builder.Default
//    @Column(name = "created_date", nullable = false)
//    private Date createdDate = new Date();

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

//    @Builder.Default
//    @ElementCollection(fetch = FetchType.EAGER)
//    private List<UserRole> roles = new ArrayList<>(List.of(USER));

//
//    @Builder.Default
//    @JsonIgnore
//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    private List<Reaction> reactions = new ArrayList<>();


    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"channel", "sender", "reply", "vote", "file", "meeting", "task", "reactions"}, allowSetters = true)
    private List<Message> messages = new ArrayList<>();

    // === Vote ===
//    @JsonIgnore
//    @Builder.Default
//    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
//    private List<Vote> votesCreated = new ArrayList<>();


    // === Choice ===
//    @JsonIgnore
//    @Builder.Default
//    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
//    private List<Choice> choicesCreated = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @ManyToMany(mappedBy = "voters", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"creator", "vote", "voters"}, allowSetters = true)
    private List<Choice> choices = new ArrayList<>();


    // === Meeting ===
//    @JsonIgnore
//    @Builder.Default
//    @OneToMany(mappedBy = "modifier", fetch = FetchType.LAZY)
//    private List<MeetingHistory> meetingHistories = new ArrayList<>();

//    @JsonIgnore
//    @Builder.Default
//    @OneToMany(mappedBy = "organizer", fetch = FetchType.LAZY)
//    private List<Meeting> meetingsOrganizer = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @ManyToMany(mappedBy = "attendees", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"organizer", "group", "histories", "attendees"}, allowSetters = true)
    private List<Meeting> meetingAttendees = new ArrayList<>();

    // === Notification ===
    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"receivers", "sender"}, allowSetters = true)
    private List<Notification> notificationsSent = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"notification", "user"}, allowSetters = true)
    private List<NotificationUser> notifications = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"user"}, allowSetters = true)
    private List<NotificationSubscriber> notificationSubscribers = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @ManyToMany(mappedBy = "recipients", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"recipients", "group"}, allowSetters = true)
    private List<Reminder> reminders = new ArrayList<>();

//    @JsonIgnore
//    @Builder.Default
//    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
//    @JsonIgnoreProperties(value = {"creator", "group", "voters"}, allowSetters = true)
//    private List<Faq> faqsCreated = new ArrayList<>();

    @Builder.Default
//    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private List<UserRole> roles = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"group", "user"}, allowSetters = true)
    private List<GroupUser> groupUsers = new ArrayList<>();

    // === Channel ===
//    @JsonIgnore
//    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
//    private List<Channel> channelsCreated;

    @JsonIgnore
    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnoreProperties(value = {"lastMessage", "creator", "group", "tasks", "votes", "meetings", "messagesPinned", "users"}, allowSetters = true)
    private List<Channel> channels;


    // === Task ===
    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "assigner", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"assigner", "group", "parentTask", "subTasks", "assignees"}, allowSetters = true)
    private List<Task> tasksAssigner = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"task", "user"}, allowSetters = true)
    private List<Assignee> tasksAssignee = new ArrayList<>();

    public boolean isPinnedGroup(String groupId) {
        return groupUsers.stream().anyMatch(groupUser -> groupUser.getGroup().getId().equals(groupId) && groupUser.isPinned());
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "email = " + email + ", " +
                "additionalEmails = " + additionalEmails + ", " +
                "imageUrl = " + imageUrl + ", " +
                "wallpaper = " + wallpaper + ", " +
                "emailVerified = " + emailVerified + ", " +
                "password = " + password + ", " +
                "provider = " + provider + ", " +
                "providerId = " + providerId + ", " +
                "status = " + status + ", " +
                "phone = " + phone + ", " +
                "birthDate = " + birthDate + ", " +
                "createdDate = " + createdDate + ", " +
                "trainingPoint = " + trainingPoint + ", " +
                "hasEnglishCert = " + hasEnglishCert + ", " +
                "studyingPoint = " + studyingPoint + ", " +
                "initialName = " + initialName + ", " +
                "gender = " + gender + ", " +
                "roles = " + roles + ")";
    }
}