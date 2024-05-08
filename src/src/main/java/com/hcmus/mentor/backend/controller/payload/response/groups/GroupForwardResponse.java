package com.hcmus.mentor.backend.controller.payload.response.groups;

import com.hcmus.mentor.backend.domain.Group;
import lombok.Builder;
import lombok.Data;

/**
 * Response for forwarding group
 */
@Data
@Builder
public class GroupForwardResponse {
    /**
     * Group id
     */
    private String id;
    /**
     * Group name
     */
    private String name;
    /**
     * Group image url
     */
    private String imageUrl;

    public static GroupForwardResponse from(Group group) {
        return GroupForwardResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .imageUrl(group.getImageUrl())
                .build();
    }
}
