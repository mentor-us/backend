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
//@Document("user")
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
    private UserGender gender = UserGender.MALE;

    @Builder.Default
    @ElementCollection
    private List<String> groupIds = new ArrayList<>();

    @Builder.Default
    @ElementCollection
    private List<String> pinnedGroupsId = new ArrayList<>();

    @Builder.Default
    @ElementCollection
    private List<UserRole> roles = new ArrayList<>(List.of(USER));

    @Builder.Default
    private Date createdDate = new Date();

    private int trainingPoint;

    private Boolean hasEnglishCert;

    private double studyingPoint;

    private String initialName;

    public User() {

    }

    public boolean isPinnedGroup(String groupId) {
        return pinnedGroupsId.contains(groupId);
    }

    public void pinGroup(String groupId) {
        if (pinnedGroupsId.contains(groupId)) {
            return;
        }
        pinnedGroupsId.add(groupId);
    }

    public void unpinGroup(String groupId) {
        if (!pinnedGroupsId.contains(groupId)) {
            return;
        }
        pinnedGroupsId.remove(groupId);
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
