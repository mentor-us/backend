package com.hcmus.mentor.backend.controller.usecase.group.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for forwarding group
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupForwardDto {

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
}
