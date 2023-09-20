package com.hcmus.mentor.backend.payload.request.groups;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddChannelRequest {

    private String channelId;

    private String channelName;

    private String groupId;
}
