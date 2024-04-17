package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends CrudRepository<Channel, String> {

    List<Channel> findByIdIn(List<String> channelIds);

    List<Channel> findByIdInAndStatusEquals(List<String> channelIds, ChannelStatus status);

//    List<Channel> findByParentIdAndTypeAndUserIdsIn(String parentId, ChannelType type, List<String> userIds);
//
    List<Channel> findByGroupId(String parentId);

//    Channel findTopByParentIdAndName(String parentId, String name);

    Optional<Channel> findByGroupIdAndName(String groupId, String name);

    boolean existsByGroupIdAndName(String parentId, String name);

//    @Aggregation(pipeline = {
//            "{ $match: { 'id': { $in: ?0 }, 'status': ?1 } }",
//            "{ $addFields: { groupObjectId: { $toObjectId: '$parentId' } } }",
//            "{ $lookup: { from: 'group', localField: 'groupObjectId', foreignField: '_id' ,as: 'groups' } }",
//            "{ $unwind: '$groups' }",
//            "{ $set: { parentId: { _id: '$groups._id', name: '$groups.name', imageUrl: '$groups.imageUrl' } } }",
//            "{ $project: { id: 1, name: 1, group: '$parentId' } }",
//            "{ $unset: 'groups' }",
//            "{ $unset: 'groupObjectId' }",
//            "{ $sort: { 'group.name': 1, 'name': 1 } }"
//
//    })
//    // TODO: Implement this method
//    List<ChannelForwardResponse> getListChannelForward(List<String> channelIds, ChannelStatus status);

//    @Query(value =
//            "SELECT c.id as channelId, c.name as channelName, g.id as groupId, g.name as groupName, g.image_url as groupImageUrl " +
//                    "FROM channel c " +
//                    "JOIN group_table g ON c.parent_id = g.id " +
//                    "WHERE c.id IN :channelIds AND c.status = :status " +
//                    "ORDER BY g.name, c.name",
//            nativeQuery = true)
//    List<ChannelForwardResponse> getListChannelForward(@Param("channelIds") List<String> channelIds, @Param("status") String status);
}
