package vn.edu.hcmus.mentor.controller.usecase.channel.getchannelsbygroupid;


import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.exception.DomainException;
import vn.edu.hcmus.mentor.domain.Channel;
import vn.edu.hcmus.mentor.repository.ChannelRepository;
import vn.edu.hcmus.mentor.repository.GroupRepository;
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

        return channelRepository.findByGroupId(query.getGroupId());
    }
}