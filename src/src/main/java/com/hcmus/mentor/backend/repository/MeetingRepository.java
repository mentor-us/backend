package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.domain.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, String> {

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
    List<MeetingResponse> findAllByGroupId(String groupId);

    Page<Meeting> findByGroupId(String groupId, PageRequest pageRequest);

    Page<Meeting> findAllByGroupIdInAndOrganizerIdAndTimeStartGreaterThanOrGroupIdInAndAttendeesInAndTimeStartGreaterThan(
            List<String> groupIds,
            String userId,
            Date startDate,
            List<String> groupId,
            List<String> ids,
            Date date,
            PageRequest pageRequest);

    @Query("SELECT m " +
            "from Meeting m " +
            "inner join m.group ch " +
            "join m.attendees attendees " +
            "WHERE (m.organizer.id = ?1 or attendees.id = ?1) and m.group.id = ?2" +
            "order by m.timeStart desc ")
    Page<Meeting> findAllOwnerMeeting(String userId, PageRequest pageRequest);

    List<Meeting> findAllByGroupIdInAndOrganizerIdOrGroupIdInAndAttendeesIn(
            List<String> activeGroupIds, String id, List<String> groupIds, List<String> ids);

    List<Meeting> findAllByGroupIdInAndOrganizerIdAndTimeStartGreaterThanEqualAndTimeEndLessThanEqual(
            List<String> groupIds, String userId, Date startDate, Date endDate);

    List<Meeting> findAllByGroupIdInAndAttendeesInAndTimeStartGreaterThanEqualAndTimeEndLessThanEqual(
            List<String> groupIds, List<String> ids, Date startDate, Date endDate);

    long countByCreatedDateBetween(Date start, Date end);

    long countByGroupId(String groupId);

    Meeting findFirstByGroupIdOrderByCreatedDateDesc(String groupId);
    Meeting findFirstByGroupIdInOrderByCreatedDateDesc(List<String> groupIds);

    long countByGroupIdAndOrganizerId(String groupId, String organizerId);

    long countByGroupIdAndAttendeesIn(String groupId, String attendeeId);

    @Query("SELECT count(m) " +
            "from Meeting m " +
            "inner join m.group ch " +
            "join m.attendees attendees " +
            "WHERE m.group.id in ?1 and (attendees.id = ?2 or m.organizer.id = ?2)")
    long countByGroupIdIdInAndIsMember(List<String> channelIds, String userId);

    Meeting findFirstByGroupIdAndOrganizerIdOrderByCreatedDateDesc(
            String groupId,
            String organizerId);

    long countByGroupIdInAndCreatedDateBetween(List<String> groupIds, Date start, Date end);

    long countByGroupIdIn(List<String> groupIds);
}
