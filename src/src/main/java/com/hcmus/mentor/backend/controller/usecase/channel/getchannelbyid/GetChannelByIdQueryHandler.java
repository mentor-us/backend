package com.hcmus.mentor.backend.controller.usecase.channel.getchannelbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupCategory;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handler for {@link GetChannelByIdQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetChannelByIdQueryHandler implements Command.Handler<GetChannelByIdQuery, GroupDetailResponse> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final ChannelRepository channelRepository;
    private final GroupRepository groupRepository;
    private final GroupCategoryRepository groupCategoryRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageService messageService;

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupDetailResponse handle(GetChannelByIdQuery query) {
        var userId = loggedUserAccessor.getCurrentUserId();

        var channel = channelRepository.findById(query.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));

        GroupDetailResponse channelDetail = fulfillChannelDetail(userId, channel, channel.getGroup());
        if (channelDetail == null) {
            throw new DomainException("Không tìm thấy kênh");
        }

        return fulfillGroupDetail(userId, channelDetail);
    }

    private GroupDetailResponse fulfillChannelDetail(
            String userId,
            Channel channel,
            Group parentGroup) {
        String channelName = channel.getName();
        String imageUrl = null;

        if (ChannelType.PRIVATE_MESSAGE.equals(channel.getType())) {
            String penpalId = channel.getUsers().stream()
                    .map(User::getId)
                    .filter(id -> !id.equals(userId))
                    .findFirst()
                    .orElse(null);
            if (userId == null) {
                return null;
            }
            ShortProfile penpal = userRepository.findShortProfile(penpalId).map(ShortProfile::new).orElse(null);
            if (penpal == null) {
                return null;
            }
            channelName = penpal.getName();
            imageUrl = penpal.getImageUrl();
        }

        GroupDetailResponse response = GroupDetailResponse.builder()
                .id(channel.getId())
                .name(channelName)
                .description(channel.getDescription())
                .pinnedMessageIds(channel.getMessagesPinned().stream().map(message -> message.getId()).toList())
                .imageUrl(imageUrl)
                .role(parentGroup.isMentor(userId) ? "MENTOR" : "MENTEE")
                .parentId(parentGroup.getId())
                .totalMember(channel.getUsers().size())
                .type(channel.getType())
                .build();

        GroupCategory groupCategory = channel.getGroup().getGroupCategory();

        if (groupCategory != null) {
            response.setPermissions(groupCategory.getPermissions());
            response.setGroupCategory(groupCategory.getName());
        }

        List<MessageResponse> messages = new ArrayList<>();

        if (response.getPinnedMessageIds() != null && !response.getPinnedMessageIds().isEmpty()) {
            messages = response.getPinnedMessageIds().stream()
                    .map(messageRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(message -> !message.isDeleted())
                    .map(message -> MessageResponse.from(message, ProfileResponse.from(message.getSender())))
                    .toList();
        }
        response.setPinnedMessages(messageService.fulfillMessages(messages, userId));
        response.setTotalMember(channel.getUsers().size());
        return response;
    }

    private GroupDetailResponse fulfillGroupDetail(String userId, GroupDetailResponse response) {
        response.setRole(userId);

        Optional<User> userWrapper = userRepository.findById(userId);
        if (userWrapper.isPresent()) {
            User user = userWrapper.get();
            response.setPinned(user.isPinnedGroup(response.getId()));
        }
        GroupCategory groupCategory = groupCategoryRepository.findByName(response.getGroupCategory());
        if (groupCategory != null) {
            response.setPermissions(groupCategory.getPermissions());
        }

        response.setPinnedMessages(fullFillPinMessages(userId, response.getPinnedMessageIds()));
        return response;
    }

    private List<MessageDetailResponse> fullFillPinMessages(String userId, List<String> pinMessageIds) {
        List<MessageResponse> messageResponses = new ArrayList<>();
        if (pinMessageIds != null && !pinMessageIds.isEmpty()) {
            messageResponses = pinMessageIds.stream()
                    .map(messageRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(message -> !message.isDeleted())
                    .map(message -> MessageResponse.from(message, ProfileResponse.from(message.getSender())))
                    .toList();
        }
        return messageService.fulfillMessages(messageResponses, userId);
    }
}