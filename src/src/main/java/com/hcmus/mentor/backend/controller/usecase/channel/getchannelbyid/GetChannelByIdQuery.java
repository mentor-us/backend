package com.hcmus.mentor.backend.controller.usecase.channel.getchannelbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
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
public class GetChannelByIdQuery implements Command<GroupDetailResponse> {
    
    /**
     * The ID of the channel to retrieve.
     */
    @NotNull
    public String id;
}
