package com.hcmus.mentor.backend.controller.usecase.channel.getchannelforward;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.channel.common.ChannelForwardDto;
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
@NoArgsConstructor
@AllArgsConstructor
public class GetChannelsForwardQuery implements Command<List<ChannelForwardDto>> {

    private String name;
}