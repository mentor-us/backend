package com.hcmus.mentor.backend.controller.payload.request.groups;

import com.hcmus.mentor.backend.domain.ChannelType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateChannelRequest {

    private String channelName;

    private String description;

    private ChannelType type;

    private List<String> userIds;
}
