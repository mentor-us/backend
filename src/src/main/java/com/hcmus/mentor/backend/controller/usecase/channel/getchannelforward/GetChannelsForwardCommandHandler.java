package com.hcmus.mentor.backend.controller.usecase.channel.getchannelforward;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.channel.common.ChannelForwardDto;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for {@link GetChannelsForwardQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetChannelsForwardCommandHandler implements Command.Handler<GetChannelsForwardQuery, List<ChannelForwardDto>> {

    private final Logger logger = LoggerFactory.getLogger(GetChannelsForwardCommandHandler.class);
    private final LoggedUserAccessor loggedUserAccessor;
    private final ChannelRepository channelRepository;

    /**
     * @param query command to get channels forward.
     * @return list of channels forward.
     */
    @Override
    public List<ChannelForwardDto> handle(GetChannelsForwardQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        return channelRepository.getListChannelForward(currentUserId, query.getName());
    }
}
