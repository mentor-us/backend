package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.domain.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends MongoRepository<Message, String> {

    @NotNull
    Optional<Message> findById(@NotNull String id);

    @NotNull
    Optional<Message> findByIdAndStatusNot(String id, Message.Status status);

//    Slice<Message> findByChannelId(String groupId, PageRequest pageRequest);

    Optional<Message> findByVoteId(String voteId);

    @Aggregation(
            pipeline = {
                    "{$match: {groupId: ?0}}",
                    "{$addFields: {senderObjectId: {$convert: {input: '$senderId', to: 'objectId', onError: '', onNull: ''}}}}",
                    "{$lookup: {from: 'user', localField: 'senderObjectId', foreignField: '_id', as: 'sender'}}",
                    "{$unwind: '$sender'}",
                    "{$sort: {createdDate: -1}}",
                    "{$skip: ?1}",
                    "{$limit: ?2}"
            })
    List<MessageResponse> getGroupMessagesByChannelId(String groupId, int offset, int size);

    @Aggregation(
            pipeline = {
                    "{$match: {groupId: ?0}}",
                    "{$match: {status: {$not: {$eq: 'DELETED'}}}}",
                    "{$match: {content: {$regex: ?1, $options: 'i'}}}",
                    "{$addFields: {senderObjectId: {$convert: {input: '$senderId', to: 'objectId', onError: '', onNull: ''}}}}",
                    "{$lookup: {from: 'user', localField: 'senderObjectId', foreignField: '_id', as: 'sender'}}",
                    "{$unwind: '$sender'}",
                    "{$sort: {createdDate: -1}}",
                    "{$skip: ?2}",
                    "{$limit: ?3}"
            })
    List<MessageResponse> findGroupMessages(String groupId, String query, int offset, int size);

    long countByCreatedDateBetween(Date start, Date end);

    long countByChannelId(String channelId);

    Message findFirstByChannelIdOrderByCreatedDateDesc(String channelId);

    long countByChannelIdAndSenderId(String groupId, String senderId);

    Message findFirstByChannelIdAndSenderIdOrderByCreatedDateDesc(String channelId, String senderId);

    long countByChannelIdInAndCreatedDateBetween(List<String> channelIds, Date start, Date end);

    long countByChannelIdIn(List<String> channelIds);

    List<Message> findByChannelIdAndTypeInAndStatusInOrderByCreatedDateDesc(
            String groupId, List<Message.Type> type, List<Message.Status> statuses);

    Optional<Message> findTopByChannelIdOrderByCreatedDateDesc(String groupId);

    @Aggregation(
            pipeline = {
                    "{$match: {groupId: ?0}}",
                    "{$addFields: {senderObjectId: {$convert: {input: '$senderId', to: 'objectId', onError: '', onNull: ''}}}}",
                    "{$lookup: {from: 'user', localField: 'senderObjectId', foreignField: '_id', as: 'sender'}}",
                    "{$unwind: '$sender'}",
                    "{$sort: {createdDate: -1}}",
            })
    List<MessageResponse> getAllGroupMessagesByChannelId(String groupId);

//    Page<Message> findByChannelId(String groupId, TextCriteria criteria, Pageable pageable);

    void deleteByChannelId(String groupId);
}
