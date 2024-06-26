package com.hcmus.mentor.backend.controller.usecase.channel.getchannelbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.controller.usecase.channel.common.ChannelDetailDto;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link GetChannelByIdQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetChannelByIdQueryHandler implements Command.Handler<GetChannelByIdQuery, ChannelDetailDto> {

    private final Logger logger = LogManager.getLogger(GetChannelByIdQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ChannelDetailDto handle(GetChannelByIdQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var channel = channelRepository.findById(query.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));
        var parentGroup = channel.getGroup();
        var groupCategory = parentGroup.getGroupCategory();

        String channelName = channel.getName();
        String imageUrl = null;

        if (ChannelType.PRIVATE_MESSAGE.equals(channel.getType())) {
            String friendId = channel.getUsers().stream()
                    .map(User::getId)
                    .filter(id -> !id.equals(currentUserId))
                    .findFirst()
                    .orElse(null);
            if (currentUserId == null) {
                return null;
            }

            ShortProfile friend = userRepository.findShortProfile(friendId)
                    .map(user -> modelMapper.map(user, ShortProfile.class)).orElse(null);
            if (friend == null) {
                return null;
            }
            channelName = friend.getName();
            imageUrl = friend.getImageUrl();
        }

        var channelDetail = modelMapper.map(channel, ChannelDetailDto.class);
        var channelMember = channel.getUsers().stream().map(User::getId).toList();

        channelDetail.setName(channelName);
        channelDetail.setImageUrl(imageUrl);
        channelDetail.setGroupCategory(groupCategory.getName());
        channelDetail.setPermissions(groupCategory.getPermissions());
        channelDetail.setMembers(channelMember);
        channelDetail.setMentors(parentGroup.getMentors().stream().map(User::getId).filter(channelMember::contains).toList());
        channelDetail.setMentees(parentGroup.getMentees().stream().map(User::getId).filter(channelMember::contains).toList());
        channelDetail.setRole(currentUserId);
        channelDetail.setPinnedMessageIds(channel.getMessagesPinned().stream().map(Message::getId).toList());
        channelDetail.setPinnedMessages(messageService.mappingToMessageDetailResponse(channel.getMessagesPinned(), currentUserId));
        channelDetail.setTimeStart(parentGroup.getTimeStart());
        channelDetail.setTimeEnd(parentGroup.getTimeEnd());

        return channelDetail;
    }

}