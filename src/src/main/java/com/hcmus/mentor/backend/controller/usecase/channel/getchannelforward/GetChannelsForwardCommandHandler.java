package com.hcmus.mentor.backend.controller.usecase.channel.getchannelforward;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.response.channel.ChannelForwardResponse;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for {@link GetChannelsForwardCommand}.
 */
@Component
@RequiredArgsConstructor
public class GetChannelsForwardCommandHandler implements Command.Handler<GetChannelsForwardCommand, List<ChannelForwardResponse>> {
    private final GroupRepository groupRepository;

    /**
     * @param command command to get channels forward.
     * @return list of channels forward.
     */
    @Override
    public List<ChannelForwardResponse> handle(GetChannelsForwardCommand command) {
        var groups = groupRepository.fetchWithUsersAndChannels(command.getUserId(), GroupStatus.ACTIVE);
        return groups.stream()
                .flatMap(group -> group.getChannels().stream())
                .filter(c -> c.getStatus() == ChannelStatus.ACTIVE)
                .map(ChannelForwardResponse::from)
                .toList();
    }
}
