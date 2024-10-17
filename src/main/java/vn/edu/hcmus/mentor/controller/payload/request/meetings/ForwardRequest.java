package vn.edu.hcmus.mentor.controller.payload.request.meetings;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ForwardRequest {
    private String messageId;
    private List<String> channelIds;
}
