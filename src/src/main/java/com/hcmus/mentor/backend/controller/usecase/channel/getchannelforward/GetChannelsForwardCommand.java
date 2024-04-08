package com.hcmus.mentor.backend.controller.usecase.channel.getchannelforward;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.response.channel.ChannelForwardResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Command to get channels forward.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetChannelsForwardCommand implements Command<List<ChannelForwardResponse>> {
    private String userId;

    private String name;
}