package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    long countByCreatedDateBetween(Date start, Date end);

    long countByChannelIdIn(List<String> channelIds);

    long countByChannelIdInAndSenderId(List<String> channelIds, String senderId);

    long countByChannelIdInAndCreatedDateBetween(List<String> groupIds, Date start, Date end);

    @NotNull
    Optional<Message> findById(@NotNull String id);

    @NotNull
    Optional<Message> findByIdAndStatusNot(String id, Message.Status status);

    Optional<Message> findByVoteId(String voteId);

    Optional<Message> findTopByChannelIdOrderByCreatedDateDesc(String groupId);

    Message findFirstByChannelIdInOrderByCreatedDateDesc(List<String> channelIds);

    List<Message> findByChannelIdAndTypeInAndStatusInOrderByCreatedDateDesc(String groupId, List<Message.Type> type, List<Message.Status> statuses);

    List<Message> getAllGroupMessagesByChannelId(String groupId);

    @Query("SELECT m, s, c " +
            "FROM Message m " +
            "join m.sender s " +
            "join m.channel c " +
            "WHERE m.channel.id = ?1 " +
//            "AND m.status != 'DELETED' " +
            "ORDER BY m.createdDate DESC ")
    List<Message> getGroupMessagesByChannelId(String groupId);

    @Query("SELECT m, s, c " +
            "FROM Message m " +
            "join m.sender s " +
            "join m.channel c " +
            "WHERE m.channel.id = ?1 " +
            "AND m.status != 'DELETED' " +
            "AND m.content LIKE %?2% " +
            "ORDER BY m.createdDate DESC ")
    List<Message> findGroupMessages(String groupId, String query);

    @Query("SELECT m, s, c " +
            "FROM Message m " +
            "join m.sender s " +
            "join m.channel c " +
            "WHERE m.channel.id IN :channelIds AND m.sender.id = :senderId " +
            "ORDER BY m.createdDate DESC " +
            "limit 1")
    Optional<Message> findLatestOwnMessageByChannel(List<String> channelIds, String senderId);

    @Modifying
    @Query("UPDATE Message m " +
            "SET m.status = :status " +
            "WHERE m.channel.id = :channelId")
    void deleteAllByChannelId(@Param(value = "channelId") String channelId, @Param(value = "status") Message.Status status);

}