package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Channel;

import java.util.List;

import com.hcmus.mentor.backend.domain.constant.ChannelType;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChannelRepository extends MongoRepository<Channel, String> {

    List<Channel> findByIdIn(List<String> channelIds);

    List<Channel> findByParentIdAndTypeAndUserIdsIn(
            String parentId, ChannelType type, List<String> userIds);

    List<Channel> findByParentId(String parentId);

    Channel findTopByParentIdAndName(String parentId, String name);

    boolean existsByParentIdAndName(String parentId, String name);

    List<Channel> findByParentIdInAndUserIdsContaining(List<String> parentIds, String userId);
}
