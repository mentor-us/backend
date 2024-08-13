package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Repository
public interface MeetingRepository extends JpaRepository<Meeting, String> {

    long countByGroupIdInAndCreatedDateBetween(List<String> groupIds, Date start, Date end);

    long countByGroupIdIn(List<String> groupIds);

    long countByCreatedDateBetween(Date start, Date end);

    Meeting findFirstByGroupIdInOrderByCreatedDateDesc(List<String> groupIds);

    Page<Meeting> findByGroupId(String groupId, Pageable pageRequest);

    List<Meeting> findAllByGroupIdInAndOrganizerIdAndTimeStartGreaterThanEqualAndTimeEndLessThanEqual(
            List<String> groupIds, String userId, Date startDate, Date endDate);

    @Query("SELECT count(m) " +
            "from Meeting m " +
            "join m.group ch " +
            "join m.attendees attendees " +
            "WHERE m.group.id in ?1 and (attendees.id = ?2 or m.organizer.id = ?2)")
    long countByGroupIdIdInAndIsMember(List<String> channelIds, String userId);

    @Query("SELECT m " +
            "FROM Meeting m " +
            "JOIN m.group ch " +
            "JOIN m.attendees att " +
            "WHERE m.isDeleted = false " +
            "AND ch.id IN ?1 " +
            "AND (att.id = ?2 OR m.organizer.id = ?2) " +
            "AND m.timeStart > ?3 " +
            "ORDER BY m.timeStart DESC")
    List<Meeting> findAllAndHasUserAndStartBefore(List<String> groupId, String userId, LocalDateTime startDate);

    @Query("SELECT m " +
            "from Meeting m " +
            "join m.group ch " +
            "join m.attendees attendees " +
            "WHERE m.group.id in ?1 and (attendees.id = ?2 or m.organizer.id = ?2)" +
            "order by m.timeStart desc ")
    List<Meeting> findAllByOwn(List<String> activeGroupIds, String id);

    @Query("SELECT m " +
            "from Meeting m " +
            "join m.group ch " +
            "join m.attendees attendees " +
            "WHERE m.group.id in ?1 and (attendees.id = ?2 ) and m.timeStart >= ?3 and m.timeEnd <= ?4")
    List<Meeting> findAllByGroupIdInAndAttendeesInAndTimeStartGreaterThanEqualAndTimeEndLessThanEqual(
            List<String> groupIds, List<String> ids, Date startDate, Date endDate);

    @Query("select m " +
            "from Meeting m " +
            "join fetch m.group  " +
            "join fetch m.organizer " +
            "where m.group.id = ?1 " +
            "order by m.createdDate desc ")
    List<Meeting> findAllByChannelId(String groupId);

    @Query("""
            SELECT m
            FROM Meeting m
            JOIN FETCH m.group ch
            JOIN FETCH ch.group gr
            WHERE gr.id = ?1
            ORDER BY m.createdDate DESC
            """)
    List<Meeting> findAllByGroupId(String groupId);
}