package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.controller.payload.response.channel.ChannelForwardResponse;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends CrudRepository<Channel, String> {

    boolean existsByGroupIdAndName(String parentId, String name);

    Optional<Channel> findByGroupIdAndName(String groupId, String name);

    List<Channel> findByIdIn(List<String> channelIds);

    List<Channel> findByGroupId(String parentId);

    List<Channel> findByIdInAndStatusEquals(List<String> channelIds, ChannelStatus status);

    @Query("SELECT c " +
            "from Channel c " +
            "inner join fetch c.users u " +
            "where c.group.id =?1 " +
            "and u.id = ?2")
    Boolean existsByIdAndUserId(String channelId, String userId);

    @Query("SELECT c " +
            "from Channel c " +
            "inner join fetch c.users u " +
            "where c.group.id in ?1 " +
            "and u.id = ?2 " +
            "and c.status = ?3" +
            "order by c.group.name, c.name")
    List<ChannelForwardResponse> getListChannelForward(List<String> channelIds, String userId, ChannelStatus status);

//    List<Channel> findAllByParentIdInAndUserIdsContaining(List<String> parentIds, String userId);

    @Query("select c,u,t " +
            "from Channel c " +
            "inner join fetch c.users u " +
            "inner join fetch c.tasks t " +
            "inner join fetch t.assignees " +
            "inner join fetch t.assigner " +
            "where u.id = ?1 and c.group.status = 'ACTIVE' and c.status = 'ACTIVE'")
    List<Channel> findOwnChannelsByUserId(String userId);
}