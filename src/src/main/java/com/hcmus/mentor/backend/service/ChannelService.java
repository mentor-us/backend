package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.controller.payload.request.groups.AddChannelRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.UpdateChannelRequest;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.security.UserPrincipal;

import java.util.List;

public interface ChannelService {
    Channel addChannel(String adderId, AddChannelRequest request);

    Channel addPrivateChat(String adderId, AddChannelRequest request, Group group);

    boolean removeChannel(UserPrincipal user, String channelId);

    List<Channel> getChannels(UserPrincipal user, String parentId);

    Channel updateChannel(UserPrincipal user, String channelId, UpdateChannelRequest request);

    List<ShortProfile> getChannelMembers(UserPrincipal user, String channelId);
}
