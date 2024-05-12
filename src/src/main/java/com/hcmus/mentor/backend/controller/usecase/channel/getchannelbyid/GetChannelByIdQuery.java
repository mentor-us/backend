package com.hcmus.mentor.backend.controller.usecase.channel.getchannelbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.channel.common.ChannelDetailDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a query to retrieve a channel by its ID.
 */
@Getter
@Setter
@Builder
public class GetChannelByIdQuery implements Command<ChannelDetailDto> {

    /**
     * The ID of the channel to retrieve.
     */
    @NotNull
    public String id;
}
