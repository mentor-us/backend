package com.hcmus.mentor.backend.controller.usecase.channel.getmembersbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for {@link GetMembersByIdQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetMembersByIdQueryHandler implements Command.Handler<GetMembersByIdQuery, List<ShortProfile>> {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ShortProfile> handle(GetMembersByIdQuery query) {
        var channel = channelRepository.findById(query.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));

        return userRepository.findByIds(channel.getUserIds());
    }
}
