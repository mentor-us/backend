package com.hcmus.mentor.backend.controller.usecase.channel.getmembersbychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for {@link GetMembersByChannelIdQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetMembersByChannelIdQueryHandler implements Command.Handler<GetMembersByChannelIdQuery, List<ShortProfile>> {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ShortProfile> handle(GetMembersByChannelIdQuery query) {
        var channel = channelRepository.findById(query.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));

        return userRepository.findByIds(channel.getUserIds());
    }
}
