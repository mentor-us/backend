package com.hcmus.mentor.backend.controller.payload.request.groups;

import com.hcmus.mentor.backend.domain.Channel;
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

    private Channel.Type type;

    private List<String> userIds;
}
