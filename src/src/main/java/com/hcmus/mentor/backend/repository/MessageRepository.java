package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.domain.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    @NotNull
    Optional<Message> findById(@NotNull String id);

    @NotNull
    Optional<Message> findByIdAndStatusNot(String id, Message.Status status);

    Slice<Message> findByGroupId(String groupId, PageRequest pageRequest);

    Optional<Message> findByVoteId(String voteId);

//    @Aggregation(
//            pipeline = {
//                    "{$match: {groupId: ?0}}",
//                    "{$addFields: {senderObjectId: {$convert: {input: '$senderId', to: 'objectId', onError: '', onNull: ''}}}}",
//                    "{$lookup: {from: 'user', localField: 'senderObjectId', foreignField: '_id', as: 'sender'}}",
//                    "{$unwind: '$sender'}",
//                    "{$sort: {createdDate: -1}}",
//                    "{$skip: ?1}",
//                    "{$limit: ?2}"
// TODO: Fix this
//            })
    List<MessageResponse> getGroupMessagesByGroupId(String groupId, int offset, int size);

//    @Aggregation(
//            pipeline = {
//                    "{$match: {groupId: ?0}}",
//                    "{$match: {status: {$not: {$eq: 'DELETED'}}}}",
//                    "{$match: {content: {$regex: ?1, $options: 'i'}}}",
//                    "{$addFields: {senderObjectId: {$convert: {input: '$senderId', to: 'objectId', onError: '', onNull: ''}}}}",
//                    "{$lookup: {from: 'user', localField: 'senderObjectId', foreignField: '_id', as: 'sender'}}",
//                    "{$unwind: '$sender'}",
//                    "{$sort: {createdDate: -1}}",
//                    "{$skip: ?2}",
//                    "{$limit: ?3}"
//            })
// TODO: Fix this
    List<MessageResponse> findGroupMessages(String groupId, String query, int offset, int size);

    long countByCreatedDateBetween(Date start, Date end);

    long countByGroupId(String groupId);
    long countByChannelIdIn(List<String> channelIds);

    Message findFirstByGroupIdOrderByCreatedDateDesc(String groupId);
    Message findFirstByChannelIdInOrderByCreatedDateDesc(List<String> channelIds);

    long countByGroupIdAndSenderId(String groupId, String senderId);
    long countByChannelIdInAndSenderId(List<String> channelIds, String senderId);

    Message findFirstByGroupIdAndSenderIdOrderByCreatedDateDesc(String groupId, String senderId);

    @Query("SELECT m " +
            "FROM Message m " +
            "WHERE m.channel.id IN :channelIds AND m.sender.id = :senderId " +
            "ORDER BY m.createdDate DESC ")
    Optional<Message> findLatestOwnMessageByChannel(List<String> channelIds, String senderId);

    long countByGroupIdInAndCreatedDateBetween(List<String> groupIds, Date start, Date end);

    long countByGroupIdIn(List<String> groupIds);

    List<Message> findByGroupIdAndTypeInAndStatusInOrderByCreatedDateDesc(
            String groupId, List<Message.Type> type, List<Message.Status> statuses);

    Optional<Message> findTopByGroupIdOrderByCreatedDateDesc(String groupId);

//    @Aggregation(
//            pipeline = {
//                    "{$match: {groupId: ?0}}",
//                    "{$addFields: {senderObjectId: {$convert: {input: '$senderId', to: 'objectId', onError: '', onNull: ''}}}}",
//                    "{$lookup: {from: 'user', localField: 'senderObjectId', foreignField: '_id', as: 'sender'}}",
//                    "{$unwind: '$sender'}",
//                    "{$sort: {createdDate: -1}}",
//            })
// TODO: Fix this
    List<MessageResponse> getAllGroupMessagesByGroupId(String groupId);

//    Page<Message> findByGroupId(String groupId, TextCriteria criteria, Pageable pageable);


    @Query("UPDATE Message m " +
            "SET m.status = :status " +
            "WHERE m.channel.id = :channelId")
    void deleteAllByChannelId(@Param(value = "channelId") String channelId, @Param(value = "status") Message.Status status);

}
