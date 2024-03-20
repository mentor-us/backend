package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.controller.payload.response.channel.ChannelForwardResponse;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelRepository extends JpaRepository<Channel, String> {

    List<Channel> findByIdIn(List<String> channelIds);

    List<Channel> findByIdInAndStatusEquals(List<String> channelIds, ChannelStatus status);

    List<Channel> findByParentIdAndTypeAndUserIdsIn(
            String parentId, ChannelType type, List<String> userIds);

    List<Channel> findByParentId(String parentId);

    Channel findTopByParentIdAndName(String parentId, String name);

    boolean existsByParentIdAndName(String parentId, String name);

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
    // TODO: Implement this method
    List<ChannelForwardResponse> getListChannelForward(List<String> channelIds, ChannelStatus status);
}
