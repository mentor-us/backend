package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.request.groups.AddChannelRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.UpdateChannelRequest;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupUser;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.MessageRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.ChannelService;
import com.hcmus.mentor.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final GroupRepository groupRepository;
    private final ChannelRepository channelRepository;
    private final PermissionService permissionService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public Channel createChannel(String creatorId, AddChannelRequest request) {
        var group = groupRepository.findById(request.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + request.getGroupId()));
        var creator =userRepository.findById(creatorId).orElseThrow(()->new DomainException("Không tìm thấy người dùng với id " + creatorId));
        if (ChannelType.PRIVATE_MESSAGE.equals(request.getType())) {
            return addPrivateChat(creatorId, request, group);
        }

        if (!group.isMentor(creatorId)) {
            throw new ForbiddenException("Chỉ có mentor được tạo kênh mới");
        }

        var name = request.getChannelName();
        if (channelRepository.existsByGroupIdAndName(group.getId(), name)) {
            throw new DomainException("Tên nhóm " + name + " đã tồn tại. Xin vui lòng tạo tên khác");
        }

        var usersInChannel = ChannelType.PUBLIC.equals(request.getType())
                ? group.getGroupUsers().stream().map(GroupUser::getUser).toList()
                : group.getGroupUsers().stream().map(GroupUser::getUser).filter(user -> request.getUserIds().contains(user.getId())).toList();

        return channelRepository.save(Channel.builder()
                .name(name)
                .description(request.getDescription())
                .type(request.getType())
                .users(usersInChannel)
                .group(group)
                .creator(creator)
                .build());
    }

    @Override
    public Channel addPrivateChat(String adderId, AddChannelRequest request, Group group) {
        request.getUserIds().add(adderId);
        List<String> memberIds = request.getUserIds().stream().distinct().sorted().toList();

        String channelName = String.join("|", memberIds) + "|" + group.getId();
        var channel = channelRepository.findByGroupIdAndName(group.getId(), channelName);
        if (channel.isPresent()) {
            return channel.get();
        }

        var users = userRepository.findByIdIn(memberIds);
        var creator = userRepository.findById(adderId).orElseThrow(() -> new DomainException("User not found"));

        return channelRepository.save(Channel.builder()
                .name(channelName)
                .description(request.getDescription())
                .type(ChannelType.PRIVATE_MESSAGE)
                .users(users)
                .group(group)
                .creator(creator)
                .build());
    }

    @Override
    public void removeChannel(CustomerUserDetails user, String channelId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new DomainException("Channel not found"));
        if (!permissionService.isMentor(user.getEmail(), channel.getGroup().getId())) {
            throw new ForbiddenException("You are not allowed to remove this channel");
        }

        Group group = channel.getGroup();
        if (group.getDefaultChannel().getId().equals(channelId)) {
            throw new DomainException("You cannot remove the default channel");
        }

        channel.setStatus(ChannelStatus.DELETED);
        channel.setDeletedDate(new Date());
        channelRepository.save(channel);
        messageRepository.deleteAllByChannelId(channel.getId(), Message.Status.DELETED);
    }

    @Override
    public List<Channel> getChannels(CustomerUserDetails user, String parentId) {
        if (parentId == null) {
            return Collections.emptyList();
        }

        Optional<Group> groupWrapper = groupRepository.findById(parentId);
        if (groupWrapper.isEmpty()) {
            return Collections.emptyList();
        }

        Group group = groupWrapper.get();
        if (!group.isMentor(user.getId())) {
            return Collections.emptyList();
        }

        return channelRepository.findByGroupId(parentId);
    }

    @Override
    public Channel updateChannel(CustomerUserDetails user, String channelId, UpdateChannelRequest request) {
        var channel = channelRepository.findById(channelId).orElseThrow(() -> new DomainException("Channel not found"));
        var group = channel.getGroup();
        if (!group.isMentor(user.getId())) {
            throw new ForbiddenException("You are not allowed to update this channel");
        }

        channel.setName(request.getChannelName());
        channel.setDescription(request.getDescription());
        channel.setType(request.getType());

        var users = userRepository.findByIdIn(request.getUserIds());
        channel.setUsers(users);

        return channelRepository.save(channel);
    }

    @Override
    public List<ShortProfile> getChannelMembers(CustomerUserDetails user, String channelId) {
        var channel = channelRepository.findById(channelId).orElseThrow(() -> new DomainException("Channel not found"));
        var group = channel.getGroup();
        if (!group.isMentor(user.getId())) {
            throw new ForbiddenException("You are not allowed to update this channel");
        }
        if (!group.isMentor(user.getId())) {
            return Collections.emptyList();
        }

        return channel.getUsers().stream().map(ShortProfile::new).toList();
    }

    @Override
    public void updateLastMessage(Channel channel, Message message){
        channel.setLastMessage(message);
        channel.ping();
        channelRepository.save(channel);

        var group = channel.getGroup();
        group.setLastMessage(message);
        group.ping();
        groupRepository.save(group);
    }
}
