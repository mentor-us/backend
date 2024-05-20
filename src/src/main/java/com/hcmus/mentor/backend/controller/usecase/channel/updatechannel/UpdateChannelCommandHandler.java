package com.hcmus.mentor.backend.controller.usecase.channel.updatechannel;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link UpdateChannelCommand}.
 */
@Component
@RequiredArgsConstructor
public class UpdateChannelCommandHandler implements Command.Handler<UpdateChannelCommand, Channel> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final ChannelRepository channelRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Channel handle(UpdateChannelCommand request) {
        var userId = loggedUserAccessor.getCurrentUserId();

        var channel = channelRepository.findById(request.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));
        var group = channel.getGroup();

        if (!group.getMentors().contains(userId)) {
            throw new ForbiddenException("Không có quyền chỉnh sửa kênh");
        }

        channel.setName(request.getChannelName());
        channel.setDescription(request.getDescription());
        channel.setType(request.getType());
        channel.setUsers(userRepository.findAllByIdIn(request.getUserIds()));

        return channelRepository.save(channel);
    }
}