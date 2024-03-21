package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.controller.payload.response.channel.ChannelForwardResponse;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface ChannelRepository extends MongoRepository<Channel, String> {

    List<Channel> findByIdIn(List<String> channelIds);

    List<Channel> findByIdInAndStatusEquals(List<String> channelIds, ChannelStatus status);

    List<Channel> findByParentIdAndTypeAndUserIdsIn(
            String parentId, ChannelType type, List<String> userIds);

    List<Channel> findByParentId(String parentId);

    List<Channel> findByParentIdInAndStatus(Collection<String> parentId, ChannelStatus status);

    Channel findTopByParentIdAndName(String parentId, String name);

    boolean existsByParentIdAndName(String parentId, String name);

    @Aggregation(pipeline = {
            "{ $match: { 'id': { $in: ?0 }, 'status': ?1 } }",
            "{ $addFields: { groupObjectId: { $toObjectId: '$parentId' } } }",
            "{ $lookup: { from: 'group', localField: 'groupObjectId', foreignField: '_id' ,as: 'groups' } }",
            "{ $unwind: '$groups' }",
            "{ $set: { parentId: { _id: '$groups._id', name: '$groups.name', imageUrl: '$groups.imageUrl' } } }",
            "{ $project: { id: 1, name: 1, group: '$parentId' } }",
            "{ $unset: 'groups' }",
            "{ $unset: 'groupObjectId' }",
            "{ $sort: { 'group.name': 1, 'name': 1 } }"

    })
    List<ChannelForwardResponse> getListChannelForward(List<String> channelIds, ChannelStatus status);

    List<Channel> findByUserIdsContainingAndStatusIs(String userId, ChannelStatus status);
}
