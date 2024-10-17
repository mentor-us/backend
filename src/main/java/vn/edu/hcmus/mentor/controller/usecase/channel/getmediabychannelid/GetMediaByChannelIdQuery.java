package vn.edu.hcmus.mentor.controller.usecase.channel.getmediabychannelid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.payload.response.ShortMediaMessage;
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
public class GetMediaByChannelIdQuery implements Command<List<ShortMediaMessage>> {

    /**
     * The ID of the media to retrieve.
     */
    private String id;
}
