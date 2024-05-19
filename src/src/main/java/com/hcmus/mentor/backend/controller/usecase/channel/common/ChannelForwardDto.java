package com.hcmus.mentor.backend.controller.usecase.channel.common;

import com.hcmus.mentor.backend.controller.usecase.group.common.GroupForwardDto;
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
    private GroupForwardDto group;

    public String getGroupName() {
        return group.getName();
    }
}
