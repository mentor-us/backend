package com.hcmus.mentor.backend.controller.payload.response.channel;

import com.hcmus.mentor.backend.controller.payload.response.groups.GroupForwardResponse;
import com.hcmus.mentor.backend.domain.Channel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChannelForwardResponse {
    private String id;
    private String name;
    private GroupForwardResponse group;

    public static ChannelForwardResponse from(Channel channel) {
        return ChannelForwardResponse.builder()
                .id(channel.getId())
                .name(channel.getName())
                .build();
    }
}
