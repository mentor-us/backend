package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.request.UpdateStudentInformationRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateUserForAdminRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateUserRequest;
import com.hcmus.mentor.backend.domain.constant.AuthProvider;
import com.hcmus.mentor.backend.domain.constant.UserGender;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import com.hcmus.mentor.backend.security.principal.oauth2.CustomerOidcUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.hcmus.mentor.backend.domain.constant.UserRole.USER;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("user")
public class User implements Serializable {

    @Id
    private String id;

    @Builder.Default
    private String name = "";
    @Email
    private String email;

    @Builder.Default
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
    private String personalEmail = "";

    @Builder.Default
    private UserGender gender = UserGender.MALE;

    @Builder.Default
    private List<String> groupIds = new ArrayList<>();

    @Builder.Default
    private List<String> pinnedGroupsId = new ArrayList<>();

    @Builder.Default
    private List<UserRole> roles = new ArrayList<>(List.of(USER));

    @Builder.Default
    private Date createdDate = new Date();

    private int trainingPoint;

    private Boolean hasEnglishCert;

    private double studyingPoint;

    private String initialName;

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
        if (!("https://graph.microsoft.com/v1.0/me/photo/$value")
                .equals(customerOidcUser.getImageUrl())) {
            imageUrl =
                    (imageUrl == null || imageUrl.equals("")) ? customerOidcUser.getImageUrl() : imageUrl;
        }
    }

    public void update(UpdateUserRequest request) {
        name = request.getName();
        imageUrl = request.getImageUrl();
        phone = request.getPhone();
        birthDate = request.getBirthDate();
        personalEmail = request.getPersonalEmail();
        gender = request.getGender();
    }

    public void update(UpdateUserForAdminRequest request) {
        name = request.getName();
        phone = request.getPhone();
        status = request.isStatus();
        birthDate = request.getBirthDate();
        personalEmail = request.getPersonalEmail();
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
