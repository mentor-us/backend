package com.hcmus.mentor.backend.controller.usecase.channel.common;

import com.hcmus.mentor.backend.controller.payload.response.groups.GroupForwardResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelForwardDto {

    private String id;
    private String name;
    private GroupForwardResponse group;

    public ChannelForwardDto(String id, String name, String groupId, String groupName, String imageUrl) {
        this.id = id;
        this.name = name;
        this.group = GroupForwardResponse.builder()
                .id(groupId)
                .name(groupName)
                .imageUrl(imageUrl)
                .build();
    }

    public String getGroupName() {
        return group.getName();
    }
}
