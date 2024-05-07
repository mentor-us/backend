package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
            "inner join m.group ch " +
            "inner join m.attendees attendees " +
            "WHERE m.group.id in ?1 and (attendees.id = ?2 or m.organizer.id = ?2)")
    long countByGroupIdIdInAndIsMember(List<String> channelIds, String userId);

    @Query("SELECT m " +
            "from Meeting m " +
            "inner join m.group ch " +
            "inner join m.attendees attendees " +
            "WHERE m.group.id in ?1 and (attendees.id = ?2 or m.organizer.id = ?2) AND m.timeStart > ?3 " )
    Page<Meeting> findAllByGroupIdInAndOrganizerIdAndTimeStartGreaterThanOrGroupIdInAndAttendeesInAndTimeStartGreaterThan(
            List<String> groupId,
            String userId,
            Date startDate,
            Pageable pageRequest);

    @Query("SELECT m " +
            "from Meeting m " +
            "inner join m.group ch " +
            "inner join m.attendees attendees " +
            "WHERE m.group.id in ?1 and (attendees.id = ?2 or m.organizer.id = ?2)" +
            "order by m.timeStart desc ")
    List<Meeting> findAllByOwn(List<String> activeGroupIds, String id);

    @Query("SELECT m " +
            "from Meeting m " +
            "inner join m.group ch " +
            "inner join m.attendees attendees " +
            "WHERE m.group.id in ?1 and (attendees.id = ?2 ) and m.timeStart >= ?3 and m.timeEnd <= ?4")
    List<Meeting> findAllByGroupIdInAndAttendeesInAndTimeStartGreaterThanEqualAndTimeEndLessThanEqual(
            List<String> groupIds, List<String> ids, Date startDate, Date endDate);

    //    @Aggregation(pipeline = {
//            "{$match: {groupId: ?0}}",
//            "{$addFields: {groupObjectId: {$toObjectId: '$groupId'}}}",
//            "{$addFields: {organizerObjectId: {$toObjectId: '$organizerId'}}}",
//            "{$lookup: {from: 'user', localField: 'organizerObjectId', foreignField: '_id', as: 'organizer'}}",
//            "{$lookup: {from: 'group', localField: 'groupObjectId', foreignField: '_id', as: 'group'}}",
//            "{'$unwind': '$group'}",
//            "{'$unwind': '$organizer'}",
//            "{'$sort':  {'createdDate':  -1}}"
//    })
    // TODO: Fix this
    List<Meeting> findAllByGroupId(String groupId);
}