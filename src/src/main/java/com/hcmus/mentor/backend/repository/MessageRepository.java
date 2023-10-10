package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.entity.Message;
import com.hcmus.mentor.backend.payload.response.messages.MessageResponse;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {

  Slice<Message> findByGroupId(String groupId, PageRequest pageRequest);

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
  List<MessageResponse> getGroupMessagesByGroupId(String groupId, int offset, int size);

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

  long countByGroupId(String groupId);

  Message findFirstByGroupIdOrderByCreatedDateDesc(String groupId);

  long countByGroupIdAndSenderId(String groupId, String senderId);

  Message findFirstByGroupIdAndSenderIdOrderByCreatedDateDesc(String groupId, String senderId);

  long countByGroupIdInAndCreatedDateBetween(List<String> groupIds, Date start, Date end);

  long countByGroupIdIn(List<String> groupIds);

  List<Message> findByGroupIdAndTypeInAndStatusInOrderByCreatedDateDesc(
      String groupId, List<Message.Type> type, List<Message.Status> statuses);

  Optional<Message> findTopByGroupIdOrderByCreatedDateDesc(String groupId);

  @Aggregation(
      pipeline = {
        "{$match: {groupId: ?0}}",
        "{$addFields: {senderObjectId: {$convert: {input: '$senderId', to: 'objectId', onError: '', onNull: ''}}}}",
        "{$lookup: {from: 'user', localField: 'senderObjectId', foreignField: '_id', as: 'sender'}}",
        "{$unwind: '$sender'}",
        "{$sort: {createdDate: -1}}",
      })
  List<MessageResponse> getAllGroupMessagesByGroupId(String groupId);

  Page<Message> findByGroupId(String groupId, TextCriteria criteria, Pageable pageable);

  void deleteByGroupId(String groupId);
}
