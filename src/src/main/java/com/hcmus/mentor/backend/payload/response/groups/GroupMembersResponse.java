package com.hcmus.mentor.backend.payload.response.groups;

import com.hcmus.mentor.backend.payload.response.users.ProfileResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupMembersResponse {

    private List<ProfileResponse> mentors;

    private List<ProfileResponse> mentees;
}
