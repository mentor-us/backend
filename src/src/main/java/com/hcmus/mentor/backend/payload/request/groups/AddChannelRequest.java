package com.hcmus.mentor.backend.payload.request.groups;

import com.hcmus.mentor.backend.entity.Channel;
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

    private Channel.Type type;

    private String groupId;

    private String creatorId;

    private List<String> userIds;
}
