package com.hcmus.mentor.backend.controller.usecase.channel.getchannelsbygroupid;


import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for {@link GetChannelsByGroupIdQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetChannelsByGroupIdQueryHandler implements Command.Handler<GetChannelsByGroupIdQuery, List<Channel>> {

    private final ChannelRepository channelRepository;
    private final GroupRepository groupRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Channel> handle(GetChannelsByGroupIdQuery query) {
        var isExist = groupRepository.existsById(query.getGroupId());

        if (!isExist) {
            throw new DomainException("Không tìm thấy group");
        }

        return channelRepository.findByParentId(query.getGroupId());
    }
}
