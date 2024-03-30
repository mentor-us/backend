package com.hcmus.mentor.backend.controller.usecase.channel.addchannel;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for {@link AddChannelCommand}.
 */
@Component
@RequiredArgsConstructor
public class AddChannelCommandHandler implements Command.Handler<AddChannelCommand, Channel> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final GroupRepository groupRepository;
    private final ChannelRepository channelRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Channel handle(AddChannelCommand command) {
        var creatorId = loggedUserAccessor.getCurrentUserId();

        var group = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm"));

        if (!group.getMentors().contains(creatorId)) {
            throw new ForbiddenException("Không có quyền thêm kênh");
        }

        if (ChannelType.PRIVATE_MESSAGE.equals(command.getType())) {
            return addPrivateChat(creatorId, command, group);
        }

        String channelName = command.getChannelName();
        if (channelRepository.existsByParentIdAndName(group.getId(), channelName)) {
            return null;
        }

        Channel data = Channel.builder()
                .name(channelName)
                .description(command.getDescription())
                .type(command.getType())
                .userIds(ChannelType.PUBLIC.equals(command.getType())
                        ? group.getMembers()
                        : command.getUserIds())
                .parentId(group.getId())
                .creatorId(command.getCreatorId())
                .build();
        Channel newChannel = channelRepository.save(data);
        group.addChannel(newChannel.getId());
        groupRepository.save(group);

        return newChannel;
    }

    private Channel addPrivateChat(String adderId, AddChannelCommand request, Group group) {
        request.getUserIds().add(adderId);
        List<String> memberIds = request.getUserIds().stream().distinct().sorted().toList();

        String channelName = String.join("|", memberIds) + "|" + group.getId();
        Channel existedChannel = channelRepository.findTopByParentIdAndName(group.getId(), channelName);
        if (existedChannel != null) {
            return existedChannel;
        }

        Channel data = Channel.builder()
                .name(channelName)
                .description(request.getDescription())
                .type(ChannelType.PRIVATE_MESSAGE)
                .userIds(memberIds)
                .parentId(group.getId())
                .creatorId(request.getCreatorId())
                .build();

        Channel newChannel = channelRepository.save(data);
        group.addPrivate(newChannel.getId());
        groupRepository.save(group);
        return newChannel;
    }
}
