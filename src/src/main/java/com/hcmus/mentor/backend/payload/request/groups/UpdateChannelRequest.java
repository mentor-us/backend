package com.hcmus.mentor.backend.payload.request.groups;

import com.hcmus.mentor.backend.entity.Channel;
import java.util.List;
import lombok.*;

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
