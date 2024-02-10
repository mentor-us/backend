package com.hcmus.mentor.backend.controller.payload.request.groups;

import com.hcmus.mentor.backend.domain.constant.ChannelType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddChannelRequest {
    private String channelName;

    private String description;

    private ChannelType type;

    private String groupId;

    private String creatorId;

    private List<String> userIds;
}
