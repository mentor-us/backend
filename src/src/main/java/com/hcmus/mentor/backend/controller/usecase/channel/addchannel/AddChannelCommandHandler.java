package com.hcmus.mentor.backend.controller.usecase.channel.addchannel;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Handler for {@link AddChannelCommand}.
 */
@Component
@RequiredArgsConstructor
public class AddChannelCommandHandler implements Command.Handler<AddChannelCommand, Channel> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final GroupRepository groupRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Channel handle(AddChannelCommand command) {
        var creatorId = loggedUserAccessor.getCurrentUserId();
        var creator = userRepository.findById(creatorId).orElseThrow(() -> new DomainException("Không tìm thấy người dùng"));

        var group = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm"));
        var members = group.getMembers().stream()
                .filter(member -> command.getType().equals(ChannelType.PUBLIC) || command.getUserIds().contains(member.getId()))
                .toList();

        Channel data = Channel.builder()
                .description(command.getDescription())
                .type(command.getType())
                .users(members)
                .group(group)
                .creator(creator)
                .build();
        if (ChannelType.PRIVATE_MESSAGE.equals(command.getType())) {
            var ch = group.getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.PRIVATE_MESSAGE))
                    .filter(channel -> new HashSet<>(channel.getUsers().stream().map(User::getId).toList()).containsAll(command.getUserIds()))
                    .filter(channel -> new HashSet<>(command.getUserIds()).containsAll(channel.getUsers().stream().map(User::getId).toList()))
                    .findFirst()
                    .orElse(null);
            if (ch != null) {
                return ch;
            }
            data.setName(String.join("|", command.getUserIds()) + "|" + group.getId());
        } else {
            if (channelRepository.existsByGroupIdAndName(group.getId(), command.getChannelName())) {
                return null;
            }
            data.setName(command.getChannelName());
        }

        return channelRepository.save(data);
    }

//    private Channel addPrivateChat(String adderId, AddChannelCommand request, Group group) {
//        request.getUserIds().add(adderId);
//        List<String> memberIds = request.getUserIds().stream().distinct().sorted().toList();
//
//        String channelName = String.join("|", memberIds) + "|" + group.getId();
//        Channel existedChannel = channelRepository.findTopByParentIdAndName(group.getId(), channelName);
//        if (existedChannel != null) {
//            return existedChannel;
//        }
//
//        Channel data = Channel.builder()
//                .name(channelName)
//                .description(request.getDescription())
//                .type(ChannelType.PRIVATE_MESSAGE)
//                .userIds(memberIds)
//                .parentId(group.getId())
//                .creatorId(request.getCreatorId())
//                .build();
//
//        Channel newChannel = channelRepository.save(data);
//        group.addPrivate(newChannel.getId());
//        groupRepository.save(group);
//        return newChannel;
//    }
}