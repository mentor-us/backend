package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.entity.Channel;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChannelRepository extends MongoRepository<Channel, String> {

  List<Channel> findByIdIn(List<String> channelIds);

  List<Channel> findByParentIdAndTypeAndUserIdsIn(
      String parentId, Channel.Type type, List<String> userIds);

  List<Channel> findByParentId(String parentId);

  Channel findTopByParentIdAndName(String parentId, String name);

  boolean existsByParentIdAndName(String parentId, String name);
}
