package com.hcmus.mentor.backend.controller.usecase.channel.getchannelforward;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.channel.common.ChannelForwardDto;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Handler for {@link GetChannelsForwardQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetChannelsForwardCommandHandler implements Command.Handler<GetChannelsForwardQuery, List<ChannelForwardDto>> {

    private final Logger logger = LoggerFactory.getLogger(GetChannelsForwardCommandHandler.class);
    private final LoggedUserAccessor loggedUserAccessor;
    private final ModelMapper mapper;
    private final ChannelRepository channelRepository;

    /**
     * @param query command to get channels forward.
     * @return list of channels forward.
     */
    @Override
    public List<ChannelForwardDto> handle(GetChannelsForwardQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        return channelRepository.getListChannelForward(currentUserId, query.getName()).stream().map(channel -> {
            var dto = mapper.map(channel, ChannelForwardDto.class);

            var name = channel.getType() == ChannelType.PRIVATE_MESSAGE ? getChannelName(channel, currentUserId) : channel.getName();
            dto.setName(name);

            return dto;
        }).filter(channel -> channel.getName().contains(query.getName())).toList();
    }

    private String getChannelName(Channel channel, String userId) {
        return channel.getUsers().stream().filter(u -> !Objects.equals(u.getId(), userId)).findFirst().map(User::getName).orElse("");
    }
}
