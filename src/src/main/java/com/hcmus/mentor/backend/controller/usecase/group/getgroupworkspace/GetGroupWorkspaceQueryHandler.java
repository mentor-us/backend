package com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Handler for {@link GetGroupWorkSpaceQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetGroupWorkspaceQueryHandler implements Command.Handler<GetGroupWorkSpaceQuery, GetGroupWorkspaceResult> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final GroupRepository groupRepository;
    private final ModelMapper modelMapper;

    /**
     * @param command command to get group detail.
     * @return result of getting group detail.
     */
    @Override
    @Transactional(readOnly = true)
    public GetGroupWorkspaceResult handle(GetGroupWorkSpaceQuery command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        Group group = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tim thấy nhóm"));

        if (group.getGroupUsers().stream().noneMatch(groupUser -> groupUser.getUser().getId().equals(currentUserId))) {
            throw new DomainException("Bạn không thể xem nhóm này");
        }

        var groupWorkspace = modelMapper.map(group, GetGroupWorkspaceResult.class);
        groupWorkspace.setMentors(group.getMentors().stream().map(User::getId).toList());
        groupWorkspace.setMentees(group.getMentees().stream().map(User::getId).toList());
        groupWorkspace.setTotalMember(group.getMembers().size());
        groupWorkspace.setRole(currentUserId);

        var allChannels = group.getChannels();
        var publicChannel = allChannels.stream()
                .filter(c -> c.getType() == ChannelType.PUBLIC)
                .map(channel -> modelMapper.map(channel, WorkspaceChannelDto.class))
                .sorted(Comparator.comparing(WorkspaceChannelDto::getUpdatedDate).reversed())
                .toList();
        groupWorkspace.setChannels(publicChannel);

        var groupUsers = group.getGroupUsers();
        var privateChannel = allChannels.stream()
                .filter(c -> c.getType() == ChannelType.PRIVATE_MESSAGE &&
                        allChannels.stream().anyMatch(ch -> ch.getUsers().stream()
                                .anyMatch(u -> Objects.equals(u.getId(), currentUserId))
                        )
                )
                .map(channel -> {
                    ShortProfile penpal = channel.getUsers().stream().filter(u -> Objects.equals(u.getId(), currentUserId)).map(ShortProfile::new).findFirst().orElse(null);
                    if (penpal == null) {
                        return null;
                    }

                    channel.setName(penpal.getName());
                    channel.setImageUrl(penpal.getImageUrl());

                    List<String> markedMentees = groupUsers.stream().filter(GroupUser::isMarked).map(gu -> gu.getUser().getId()).toList();

                    var workspaceChannelDto = modelMapper.map(channel, WorkspaceChannelDto.class);
                    workspaceChannelDto.setMarked(markedMentees.contains(penpal.getId()));

                    return workspaceChannelDto;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(WorkspaceChannelDto::getUpdatedDate).reversed())
                .sorted(Comparator.comparing(WorkspaceChannelDto::getMarked).reversed())
                .toList();
        groupWorkspace.setPrivates(privateChannel);

        return groupWorkspace;
    }
}