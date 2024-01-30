package com.hcmus.mentor.backend.controller.payload.request.meetings;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForwardRequest {
    private String messageId;
    private List<String> channelIds;
}
