package com.hcmus.mentor.backend.controller.usecase.channel.addchannel;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
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
    private final AuditRecordService auditRecordService;

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

        var channel = channelRepository.save(data);
        auditRecordService.save(AuditRecord.builder()
                .action(ActionType.CREATED)
                .domain(DomainType.CHANNEL)
                .user(creator)
                .detail(String.format("Tạo kênh %s thuộc nhóm %s", data.getName(), group.getName()))
                .entityId(channel.getId())
                .build());

        return channel;
    }
}