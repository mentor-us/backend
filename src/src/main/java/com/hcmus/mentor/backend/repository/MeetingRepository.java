package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.domain.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface MeetingRepository extends MongoRepository<Meeting, String> {

    @Aggregation(pipeline = {
            "{$match: {groupId: ?0}}",
            "{$addFields: {groupObjectId: {$toObjectId: '$groupId'}}}",
            "{$addFields: {organizerObjectId: {$toObjectId: '$organizerId'}}}",
            "{$lookup: {from: 'user', localField: 'organizerObjectId', foreignField: '_id', as: 'organizer'}}",
            "{$lookup: {from: 'group', localField: 'groupObjectId', foreignField: '_id', as: 'group'}}",
            "{'$unwind': '$group'}",
            "{'$unwind': '$organizer'}",
            "{'$sort':  {'createdDate':  -1}}"
    })
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

    List<Meeting> findAllByGroupIdInAndOrganizerIdOrGroupIdInAndAttendeesIn(
            List<String> activeGroupIds, String id, List<String> groupIds, List<String> ids);

    List<Meeting> findAllByGroupIdInAndOrganizerIdAndTimeStartGreaterThanEqualAndTimeEndLessThanEqual(
            List<String> groupIds, String userId, Date startDate, Date endDate);

    List<Meeting> findAllByGroupIdInAndAttendeesInAndTimeStartGreaterThanEqualAndTimeEndLessThanEqual(
            List<String> groupIds, List<String> ids, Date startDate, Date endDate);

    long countByCreatedDateBetween(Date start, Date end);

    long countByGroupId(String groupId);

    Meeting findFirstByGroupIdOrderByCreatedDateDesc(String groupId);

    long countByGroupIdAndOrganizerId(String groupId, String organizerId);

    long countByGroupIdAndAttendeesIn(String groupId, String attendeeId);

    Meeting findFirstByGroupIdAndOrganizerIdOrderByCreatedDateDesc(
            String groupId,
            String organizerId);

    long countByGroupIdInAndCreatedDateBetween(List<String> groupIds, Date start, Date end);

    long countByGroupIdIn(List<String> groupIds);
}
