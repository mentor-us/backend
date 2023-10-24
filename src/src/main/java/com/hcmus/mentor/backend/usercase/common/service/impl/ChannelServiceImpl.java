package com.hcmus.mentor.backend.usercase.common.service.impl;

import com.hcmus.mentor.backend.entity.Channel;
import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.payload.request.groups.AddChannelRequest;
import com.hcmus.mentor.backend.payload.request.groups.UpdateChannelRequest;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.usercase.common.repository.ChannelRepository;
import com.hcmus.mentor.backend.usercase.common.repository.GroupRepository;
import com.hcmus.mentor.backend.usercase.common.repository.MessageRepository;
import com.hcmus.mentor.backend.usercase.common.repository.UserRepository;
import com.hcmus.mentor.backend.web.infrastructure.security.UserPrincipal;
import com.hcmus.mentor.backend.usercase.common.service.ChannelService;
import com.hcmus.mentor.backend.usercase.common.service.PermissionService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    Group group = null;
    if (!groupWrapper.isPresent()) {
      Optional<Channel> channelWrapper = channelRepository.findById(request.getGroupId());
      if (!channelWrapper.isPresent()) {
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

    if (Channel.Type.PRIVATE_MESSAGE.equals(request.getType())) {
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
                Channel.Type.PUBLIC.equals(request.getType())
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
    List<String> memberIds =
        request.getUserIds().stream().distinct().sorted().collect(Collectors.toList());

    String channelName = String.join("|", memberIds) + "|" + group.getId();
    Channel existedChannel = channelRepository.findTopByParentIdAndName(group.getId(), channelName);
    if (existedChannel != null) {
      return existedChannel;
    }

    Channel data =
        Channel.builder()
            .name(channelName)
            .description(request.getDescription())
            .type(Channel.Type.PRIVATE_MESSAGE)
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
  public boolean removeChannel(UserPrincipal user, String channelId) {
    Optional<Channel> channelWrapper = channelRepository.findById(channelId);
    if (!channelWrapper.isPresent()) {
      return true;
    }

    Channel channel = channelWrapper.get();
    if (!permissionService.isMentor(user.getEmail(), channel.getParentId())) {
      return false;
    }

    channelRepository.delete(channel);
    messageRepository.deleteByGroupId(channel.getId());

    Optional<Group> groupWrapper = groupRepository.findById(channel.getParentId());
    if (groupWrapper.isPresent()) {
      Group group = groupWrapper.get();
      group.removeChannel(channelId);
      groupRepository.save(group);
    }
    return true;
  }

  @Override
  public List<Channel> getChannels(UserPrincipal user, String parentId) {
    if (parentId == null) {
      return Collections.emptyList();
    }

    Optional<Group> groupWrapper = groupRepository.findById(parentId);
    if (!groupWrapper.isPresent()) {
      return Collections.emptyList();
    }

    Group group = groupWrapper.get();
    if (!group.isMentor(user.getId())) {
      return Collections.emptyList();
    }

    return channelRepository.findByParentId(parentId);
  }

  @Override
  public Channel updateChannel(UserPrincipal user, String channelId, UpdateChannelRequest request) {
    Optional<Channel> channelWrapper = channelRepository.findById(channelId);
    if (!channelWrapper.isPresent()) {
      return null;
    }

    Channel channel = channelWrapper.get();
    Optional<Group> groupWrapper = groupRepository.findById(channel.getParentId());
    if (!groupWrapper.isPresent()) {
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
  public List<ShortProfile> getChannelMembers(UserPrincipal user, String channelId) {
    Optional<Channel> channelWrapper = channelRepository.findById(channelId);
    if (!channelWrapper.isPresent()) {
      return null;
    }

    Channel channel = channelWrapper.get();
    Optional<Group> groupWrapper = groupRepository.findById(channel.getParentId());
    if (!groupWrapper.isPresent()) {
      return null;
    }

    Group group = groupWrapper.get();
    if (!group.isMentor(user.getId())) {
      return null;
    }

    return userRepository.findByIds(channel.getUserIds());
  }
}
