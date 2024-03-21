package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.request.groups.AddChannelRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.UpdateChannelRequest;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
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

    @Override
    public Channel addChannel(String adderId, AddChannelRequest request) {
        Optional<Group> groupWrapper = groupRepository.findById(request.getGroupId());
        Group group;
        if (groupWrapper.isEmpty()) {
            Optional<Channel> channelWrapper = channelRepository.findById(request.getGroupId());
            if (channelWrapper.isEmpty()) {
                return null;
            }
            Channel channel = channelWrapper.get();
            group = groupRepository.findById(channel.getParentId()).orElse(null);
        } else {
            group = groupWrapper.get();
        }
        if (group == null) {
            return null;
        }

        if (ChannelType.PRIVATE_MESSAGE.equals(request.getType())) {
            return addPrivateChat(adderId, request, group);
        }

        if (!group.isMentor(adderId)) {
            return null;
        }

        String channelName = request.getChannelName();
        if (channelRepository.existsByParentIdAndName(group.getId(), channelName)) {
            return null;
        }

        Channel data =
                Channel.builder()
                        .name(channelName)
                        .description(request.getDescription())
                        .type(request.getType())
                        .userIds(
                                ChannelType.PUBLIC.equals(request.getType())
                                        ? group.getMembers()
                                        : request.getUserIds())
                        .parentId(group.getId())
                        .creatorId(request.getCreatorId())
                        .build();
        Channel newChannel = channelRepository.save(data);
        group.addChannel(newChannel.getId());
        groupRepository.save(group);
        return newChannel;
    }

    @Override
    public Channel addPrivateChat(String adderId, AddChannelRequest request, Group group) {
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

    @Override
    public void removeChannel(CustomerUserDetails user, String channelId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new DomainException("Channel not found"));
        if (!permissionService.isMentor(user.getEmail(), channel.getParentId())) {
            throw new ForbiddenException("You are not allowed to remove this channel");
        }

        Group group = groupRepository.findById(channel.getParentId()).orElseThrow(() -> new DomainException("Group not found"));
        if (group.getDefaultChannelId().equals(channelId)) {
            throw new DomainException("You cannot remove the default channel");
        }

        group.removeChannel(channelId);
        groupRepository.save(group);

        channelRepository.delete(channel);
        messageRepository.deleteByChannelId(channel.getId());
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

        return channelRepository.findByParentId(parentId);
    }

    @Override
    public Channel updateChannel(CustomerUserDetails user, String channelId, UpdateChannelRequest request) {
        Optional<Channel> channelWrapper = channelRepository.findById(channelId);
        if (channelWrapper.isEmpty()) {
            return null;
        }

        Channel channel = channelWrapper.get();
        Optional<Group> groupWrapper = groupRepository.findById(channel.getParentId());
        if (groupWrapper.isEmpty()) {
            return null;
        }

        Group group = groupWrapper.get();
        if (!group.isMentor(user.getId())) {
            return null;
        }

        channel.update(request);
        return channelRepository.save(channel);
    }

    @Override
    public List<ShortProfile> getChannelMembers(CustomerUserDetails user, String channelId) {
        Optional<Channel> channelWrapper = channelRepository.findById(channelId);
        if (channelWrapper.isEmpty()) {
            return Collections.emptyList();
        }

        Channel channel = channelWrapper.get();
        Optional<Group> groupWrapper = groupRepository.findById(channel.getParentId());
        if (groupWrapper.isEmpty()) {
            return Collections.emptyList();
        }

        Group group = groupWrapper.get();
        if (!group.isMentor(user.getId())) {
            return Collections.emptyList();
        }

        return userRepository.findByIds(channel.getUserIds());
    }
}
