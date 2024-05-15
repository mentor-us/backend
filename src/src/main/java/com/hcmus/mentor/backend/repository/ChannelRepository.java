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

    @Query(nativeQuery = true, value = "select case when (count(*) > 0) then true else false end from (select 1 from rel_user_channel where channel_id = ?1 and user_id = ?2)")
    boolean existsByIdAndUserId(String channelId, String userId);

    @Query("SELECT c " +
            "from Channel c " +
            "inner join fetch c.users u " +
            "where c.group.id in ?1 " +
            "and u.id = ?2 " +
            "and c.status = ?3" +
            "order by c.group.name, c.name")
    List<ChannelForwardResponse> getListChannelForward(List<String> channelIds, String userId, ChannelStatus status);

    @Query("select c,u " +
            "from Channel c " +
            "join fetch c.users u " +
            "where u.id = ?1 and c.group.status = 'ACTIVE' and c.status = 'ACTIVE'")
    List<Channel> findOwnChannelsByUserId(String userId);

    @Query("select c " +
            "from Channel c " +
            "join c.group g " +
            "join g.groupUsers gu " +
            "join c.users cu " +
            "where c.id = ?1 and gu.id = ?2 and gu.isMentor = true and cu.id = ?2")
    Boolean existsMentorInChannel(String channelId, String userId);
}