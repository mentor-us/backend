package com.hcmus.mentor.backend.usercase.common.service;

import com.hcmus.mentor.backend.entity.Channel;
import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.payload.request.groups.AddChannelRequest;
import com.hcmus.mentor.backend.payload.request.groups.UpdateChannelRequest;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.web.infrastructure.security.UserPrincipal;
import java.util.List;

public interface ChannelService {
  Channel addChannel(String adderId, AddChannelRequest request);

  Channel addPrivateChat(String adderId, AddChannelRequest request, Group group);

  boolean removeChannel(UserPrincipal user, String channelId);

  List<Channel> getChannels(UserPrincipal user, String parentId);

  Channel updateChannel(UserPrincipal user, String channelId, UpdateChannelRequest request);

  List<ShortProfile> getChannelMembers(UserPrincipal user, String channelId);
}
