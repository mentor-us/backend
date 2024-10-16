package com.hcmus.mentor.backend.controller.usecase.user.searchmenteesofuser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortMenteeProfile {

    private String id;
    private String name;
    private String email;
    private String imageUrl;
}