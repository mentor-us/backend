package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends CrudRepository<Channel, String> {

    @Query("select exists (select c " +
            "from Channel c " +
            "where c.group.id = ?1 and c.name = ?2 and c.status != 'DELETED')")
    boolean existsByGroupIdAndName(String parentId, String name);

    Optional<Channel> findByGroupIdAndName(String groupId, String name);

    List<Channel> findByIdIn(List<String> channelIds);

    @Query("select c " +
            "from Channel c " +
            "where c.group.id = ?1 and c.status = 'ACTIVE'")
    List<Channel> findByGroupId(String parentId);

    List<Channel> findByIdInAndStatusEquals(List<String> channelIds, ChannelStatus status);

    @Query("select exists (select c " +
            "from Channel c " +
            "inner join c.users cu " +
            "where cu.id = ?2 and c.id = ?1 and c.status = 'ACTIVE')")
    boolean existsByIdAndUserId(String channelId, String userId);

    @Query("SELECT c " +
            "FROM Channel c " +
            "INNER JOIN c.users u " +
            "WHERE u.id = ?1 " +
            "AND c.name like %?2% " +
            "AND c.group.status = 'ACTIVE' and c.status = 'ACTIVE' " +
            "ORDER BY c.group.name, c.name")
    List<Channel> getListChannelForward(String userId, String name);

    @Query("select c,u " +
            "from Channel c " +
            "join fetch c.users u " +
            "where u.id = ?1 and c.group.status = 'ACTIVE' and c.status = 'ACTIVE'")
    List<Channel> findOwnActivateChannelsByUserId(String userId);

    @Query("select c,u " +
            "from Channel c " +
            "join fetch c.users u " +
            "where u.id = ?1")
    List<Channel> findOwnChannelsByUserId(String userId);

    @Query("select exists (select c " +
            "from Channel c " +
            "inner join c.group g " +
            "inner join g.groupUsers gu " +
            "where c.id = ?1 and gu.user.id = ?2 and gu.isMentor = true and c.status != 'DELETED')")
    boolean existsMentorInChannel(String channelId, String userId);
}