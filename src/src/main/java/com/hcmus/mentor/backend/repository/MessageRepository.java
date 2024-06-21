package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.repository.custom.MessageRepositoryCustom;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String>, MessageRepositoryCustom {

    long countByCreatedDateBetween(Date start, Date end);

    long countByChannelIdIn(List<String> channelIds);

    long countByChannelIdInAndSenderId(List<String> channelIds, String senderId);

    long countByChannelIdInAndCreatedDateBetween(List<String> groupIds, Date start, Date end);

    @NotNull
    Optional<Message> findById(@NotNull String id);

    @NotNull
    Optional<Message> findByIdAndStatusNot(String id, Message.Status status);

    Optional<Message> findByVoteId(String voteId);

    Optional<Message> findByTaskId(String taskId);

    Optional<Message> findByMeetingId(String meetingId);

    Optional<Message> findTopByChannelIdOrderByCreatedDateDesc(String groupId);

    Message findFirstByChannelIdInOrderByCreatedDateDesc(List<String> channelIds);

    List<Message> findByChannelIdAndTypeInAndStatusInOrderByCreatedDateDesc(String groupId, List<Message.Type> type, List<Message.Status> statuses);

    List<Message> getAllGroupMessagesByChannelId(String groupId);

    @Query(value = "SELECT DISTINCT me.* " +
            "FROM messages me " +
            "JOIN users u ON me.sender_id = u.id " +
            "LEFT JOIN tasks t ON me.task_id = t.id " +
            "LEFT JOIN task_assignee ta ON t.id = ta.task_id " +
            "LEFT JOIN meetings mt ON me.meeting_id = mt.id " +
            "LEFT JOIN rel_user_meeting_attendees ma ON mt.id = ma.meeting_id " +
            "LEFT JOIN channels c ON me.channel_id = c.id " +
            "LEFT JOIN public.files f on me.file_id = f.id " +
            "WHERE me.channel_id = ?1 " +
            "AND (CASE " +
            "WHEN me.type = 'TASK' THEN t.assigner_id = ?2 OR ta.user_id = ?2 " +
            "WHEN me.type = 'MEETING' THEN mt.organizer_id = ?2 OR ma.user_id = ?2 " +
            "ELSE TRUE END ) " +
            "ORDER BY me.created_date DESC", nativeQuery = true)
    Page<Message> getGroupMessagesByChannelId(String groupId, String viewId, Pageable pageable);

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

    List<Message> findByIdIn(List<String> ids);
}