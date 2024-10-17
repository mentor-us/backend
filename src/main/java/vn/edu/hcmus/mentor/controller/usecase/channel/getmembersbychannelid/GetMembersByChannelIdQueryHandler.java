package vn.edu.hcmus.mentor.controller.usecase.channel.getmembersbychannelid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.exception.DomainException;
import vn.edu.hcmus.mentor.controller.payload.response.users.ShortProfile;
import vn.edu.hcmus.mentor.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for {@link GetMembersByChannelIdQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetMembersByChannelIdQueryHandler implements Command.Handler<GetMembersByChannelIdQuery, List<ShortProfile>> {

    private final ChannelRepository channelRepository;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ShortProfile> handle(GetMembersByChannelIdQuery query) {
        var channel = channelRepository.findById(query.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));

        return channel.getUsers().stream()
                .map(user -> modelMapper.map(user, ShortProfile.class))
                .toList();
    }
}