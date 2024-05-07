package com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
public class GetGroupWorkspaceQueryHandler implements Command.Handler<GetGroupWorkSpaceQuery, GroupDetailResponse> {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    /**
     * @param command command to get group detail.
     * @return result of getting group detail.
     */
    @Override
    @Transactional(readOnly = true)
    public GroupDetailResponse handle(GetGroupWorkSpaceQuery command) {
        Group group = groupRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tim thấy nhóm"));

        if (group.getGroupUsers().stream().noneMatch(groupUser -> groupUser.getUser().getId().equals(command.getCurrentUserId()))) {
            throw new DomainException("Bạn không thể xem nhóm này");
        }

        var groupUsers = group.getGroupUsers();

        var groupDetailResponse = new GroupDetailResponse(group);
        var channels = group.getChannels();

        groupDetailResponse.setChannels(channels.stream()
                .filter(c -> c.getType() == ChannelType.PUBLIC)
                .map(GroupDetailResponse.GroupChannel::from)
                .sorted(Comparator.comparing(GroupDetailResponse.GroupChannel::getUpdatedDate).reversed())
                .toList());

        groupDetailResponse.setPrivates(channels.stream()
                .filter(c -> c.getType() == ChannelType.PRIVATE_MESSAGE && channels.stream()
                        .anyMatch(ch -> ch.getUsers().stream()
                                .anyMatch(u -> Objects.equals(u.getId(), command.getCurrentUserId()))))
                .map(channel -> {
                    ShortProfile penpal = channel.getUsers().stream().filter(u -> Objects.equals(u.getId(), command.getCurrentUserId())).map(ShortProfile::new).findFirst().orElse(null);
                    if (penpal == null) {
                        return null;
                    }
                    channel.setName(penpal.getName());
                    channel.setImageUrl(penpal.getImageUrl());

                    List<String> markedMentees = groupUsers.stream().filter(GroupUser::isMarked).map(gu -> gu.getUser().getId()).toList();
                    return GroupDetailResponse.GroupChannel.from(channel, markedMentees.contains(penpal.getId()));
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(GroupDetailResponse.GroupChannel::getUpdatedDate).reversed())
                .sorted(Comparator.comparing(GroupDetailResponse.GroupChannel::getMarked).reversed())
                .toList());

        return groupDetailResponse;
    }
}