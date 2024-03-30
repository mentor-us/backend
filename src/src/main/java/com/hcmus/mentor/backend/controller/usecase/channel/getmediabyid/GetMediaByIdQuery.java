package com.hcmus.mentor.backend.controller.usecase.channel.getmediabyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.response.ShortMediaMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a query to retrieve media by its ID.
 */
@Getter
@Setter
@Builder
public class GetMediaByIdQuery implements Command<List<ShortMediaMessage>> {

    /**
     * The ID of the media to retrieve.
     */
    private String id;
}
