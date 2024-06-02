package com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupWorkspaceDto;
import com.hcmus.mentor.backend.controller.usecase.group.common.WorkspaceChannelDto;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Handler for {@link GetGroupWorkSpaceQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetGroupWorkspaceQueryHandler implements Command.Handler<GetGroupWorkSpaceQuery, GroupWorkspaceDto> {

    private final Logger logger = LogManager.getLogger(GetGroupWorkspaceQueryHandler.class);
    private final LoggedUserAccessor loggedUserAccessor;
    private final GroupRepository groupRepository;
    private final ModelMapper modelMapper;

    /**
     * @param command command to get group detail.
     * @return result of getting group detail.
     */
    @Override
    @Transactional(readOnly = true)
    public GroupWorkspaceDto handle(GetGroupWorkSpaceQuery command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        Group group = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tim thấy nhóm"));

        if (!group.isMember(currentUserId)) {
            throw new DomainException("Bạn không thể xem nhóm này");
        }

        var groupWorkspace = modelMapper.map(group, GroupWorkspaceDto.class);
        groupWorkspace.setRole(currentUserId);

        var allChannels = group.getChannels().stream().filter(c -> c.getStatus() == ChannelStatus.ACTIVE).toList();
        var publicChannel = allChannels.stream()
                .filter(c -> !Objects.equals(c.getId(), group.getDefaultChannel().getId()))
                .filter(c -> c.getType() == ChannelType.PUBLIC || c.getType() == ChannelType.PRIVATE)
                .filter(c -> c.isMember(currentUserId))
                .map(channel -> modelMapper.map(channel, WorkspaceChannelDto.class))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(WorkspaceChannelDto::getUpdatedDate).reversed())
                .toList();
        groupWorkspace.setChannels(publicChannel);

        var groupUsers = group.getGroupUsers();
        var privateChannel = allChannels.stream()
                .filter(c -> !Objects.equals(c.getId(), group.getDefaultChannel().getId()))
                .filter(c -> c.getType() == ChannelType.PRIVATE_MESSAGE)
                .filter(c -> c.isMember(currentUserId))
                .map(mapToWorkspaceChannelDto(currentUserId, groupUsers))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(WorkspaceChannelDto::getUpdatedDate).reversed())
                .sorted(Comparator.comparing(WorkspaceChannelDto::getMarked).reversed())
                .toList();
        groupWorkspace.setPrivates(privateChannel);

        return groupWorkspace;
    }

    private @NotNull Function<Channel, WorkspaceChannelDto> mapToWorkspaceChannelDto(String currentUserId, Set<GroupUser> groupUsers) {
        return channel -> {
            ShortProfile friendId = channel.getUsers().stream()
                    .filter(u -> !Objects.equals(u.getId(), currentUserId))
                    .map(u -> modelMapper.map(u, ShortProfile.class))
                    .findFirst()
                    .orElse(null);

            if (friendId == null) {
                return null;
            }

            channel.setName(friendId.getName());
            channel.setImageUrl(friendId.getImageUrl());

            List<String> markedMentees = groupUsers.stream().filter(GroupUser::isMarked).map(gu -> gu.getUser().getId()).toList();

            var workspaceChannelDto = modelMapper.map(channel, WorkspaceChannelDto.class);
            workspaceChannelDto.setMarked(markedMentees.contains(friendId.getId()));

            return workspaceChannelDto;
        };
    }
}