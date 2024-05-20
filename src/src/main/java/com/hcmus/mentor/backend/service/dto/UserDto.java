package com.hcmus.mentor.backend.service.dto;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.AuthProvider;
import com.hcmus.mentor.backend.domain.constant.UserGender;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private String id;

    @Builder.Default
    private String name = "";

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
    private List<UserRole> roles = new ArrayList<>();

    @Builder.Default
    private Date createdDate = new Date();

    private int trainingPoint;

    private Boolean hasEnglishCert;

    private double studyingPoint;

    private String initialName;

    public static UserDto from(User userDto) {
        return UserDto.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .additionalEmails(userDto.getAdditionalEmails())
                .imageUrl(userDto.getImageUrl())
                .wallpaper(userDto.getWallpaper())
                .emailVerified(userDto.getEmailVerified())
                .password(userDto.getPassword())
                .provider(userDto.getProvider())
                .providerId(userDto.getProviderId())
                .status(userDto.isStatus())
                .phone(userDto.getPhone())
                .birthDate(userDto.getBirthDate())
                .gender(userDto.getGender())
                .roles(userDto.getRoles())
                .createdDate(userDto.getCreatedDate())
                .trainingPoint(userDto.getTrainingPoint())
                .hasEnglishCert(userDto.getHasEnglishCert())
                .studyingPoint(userDto.getStudyingPoint())
                .initialName(userDto.getInitialName())
                .build();
    }
}