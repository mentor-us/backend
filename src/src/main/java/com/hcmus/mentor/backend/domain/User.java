package com.hcmus.mentor.backend.domain;

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
@JsonIgnoreProperties(value = {
        "messages", "choices", "meetingAttendees", "notificationsSent", "notifications", "notificationSubscribers", "reminders", "faqs",
        "groupUsers", "channels", "tasksAssigner", "tasksAssignee", "hibernateLazyInitializer", "handler", "noteHistories", "note"},
        allowSetters = true)
public class User extends BaseDomain implements Serializable {

    @Column(name = "name", nullable = false)
    private String name;

    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
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
    @ElementCollection(fetch = FetchType.LAZY)
    private List<UserRole> roles = new ArrayList<>();

    //------------------------------------------------------------------------------------------------------------------
    // === Messge ===
    @Builder.Default
    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();

    //------------------------------------------------------------------------------------------------------------------
    // === Choice ===
    @Builder.Default
    @ManyToMany(mappedBy = "voters", fetch = FetchType.LAZY)
    private List<Choice> choices = new ArrayList<>();

    // === Meeting ===
    @Builder.Default
    @ManyToMany(mappedBy = "attendees", fetch = FetchType.LAZY)
    private List<Meeting> meetingAttendees = new ArrayList<>();

    //------------------------------------------------------------------------------------------------------------------
    // === Notification ===
    @Builder.Default
    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
    private List<Notification> notificationsSent = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"notification", "user"}, allowSetters = true)
    private List<NotificationUser> notifications = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<NotificationSubscriber> notificationSubscribers = new ArrayList<>();

    //------------------------------------------------------------------------------------------------------------------
    // === Reminder ===
    @Builder.Default
    @ManyToMany(mappedBy = "recipients", fetch = FetchType.LAZY)
    private List<Reminder> reminders = new ArrayList<>();

    //------------------s------------------------------------------------------------------------------------------------
    // === Group ===
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<GroupUser> groupUsers = new ArrayList<>();

    //------------------------------------------------------------------------------------------------------------------
    // === Channel ===
    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    private List<Channel> channels;

    //------------------------------------------------------------------------------------------------------------------
    // === Task ===
    @Builder.Default
    @OneToMany(mappedBy = "assigner", fetch = FetchType.LAZY)
    private List<Task> tasksAssigner = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Assignee> tasksAssignee = new ArrayList<>();

    //------------------------------------------------------------------------------------------------------------------
    // === Note ===
//    @Builder.Default
//    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
//    private List<Note> notes = new ArrayList<>();
//
//    @Builder.Default
//    @OneToMany(mappedBy = "modifier", fetch = FetchType.LAZY)
//    private List<NoteHistory> noteHistories = new ArrayList<>();

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