package com.hcmus.mentor.backend.controller.payload.response.groups;

import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupMembersResponse {

    private List<GroupMember> mentors;

    private List<GroupMember> mentees;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class GroupMember {
        private String id;
        private String name;
        private String email;
        private String imageUrl;
        private String role;
        private Boolean marked;

        public static GroupMember from(ProfileResponse profile, String role) {
            return from(profile, role, null);
        }

        public static GroupMember from(ProfileResponse profile, String role, Boolean marked) {
            return GroupMember.builder()
                    .id(profile.getId())
                    .name(profile.getName())
                    .email(profile.getEmail())
                    .imageUrl(profile.getImageUrl())
                    .role(role)
                    .marked(marked)
                    .build();
        }
    }
}
