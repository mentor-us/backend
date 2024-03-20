package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.controller.payload.request.groups.AddChannelRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.UpdateChannelRequest;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;

import java.util.List;

public interface ChannelService {
    Channel createChannel(String creatorId, AddChannelRequest request);

    Channel addPrivateChat(String adderId, AddChannelRequest request, Group group);

    void removeChannel(CustomerUserDetails user, String channelId);

    List<Channel> getChannels(CustomerUserDetails user, String parentId);

    Channel updateChannel(CustomerUserDetails user, String channelId, UpdateChannelRequest request);

    List<ShortProfile> getChannelMembers(CustomerUserDetails user, String channelId);
}
