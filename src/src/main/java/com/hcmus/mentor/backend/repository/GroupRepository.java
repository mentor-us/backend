package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface GroupRepository extends MongoRepository<Group, String> {
    Page<Group> findAllOrderByCreatedDate(Pageable pageable);

    List<Group> findAllByOrderByCreatedDate();

    boolean existsByName(String s);

    boolean existsByIdAndMentorsIn(String groupId, String userId);

    boolean existsByIdAndMenteesIn(String groupId, String userId);

    Group findByName(String name);

    List<Group> findAllByMentorsIn(List<String> mentorIds);

    List<Group> findAllByMenteesIn(List<String> menteeIds);

    Page<Group> findAllByMentorsIn(List<String> mentorIds, Pageable pageable);

    Page<Group> findAllByMentorsInAndStatus(
            List<String> mentorIds, GroupStatus status, Pageable pageable);

    Page<Group> findAllByMenteesIn(List<String> menteeIds, Pageable pageable);

    Page<Group> findAllByMenteesInAndStatus(
            List<String> menteeIds, GroupStatus status, Pageable pageable);

    Page<Group> findAllByMenteesIn(String menteeId, Pageable pageable);

    List<Group> findAllByMenteesIn(String menteeId);

    List<Group> findAllByMentorsIn(String mentorId);

    List<Group> findAllByMenteesInOrMentorsIn(String userId);

    Page<Group> findAllByMentorsIn(String mentorId, Pageable pageable);

    Page<Group> findAllByNameLikeIgnoreCase(String name, Pageable pageable);

    Page<Group> findAllByMentorsInOrMenteesIn(
            List<String> mentorIds, List<String> menteeIds, Pageable pageable);

    Slice<Group> findByMentorsInAndStatusOrMenteesInAndStatus(
            List<String> mentorIds,
            GroupStatus status1,
            List<String> menteeIds,
            GroupStatus status2,
            Pageable pageable);

    List<Group> findByMentorsInAndStatusOrMenteesInAndStatus(
            List<String> mentorIds, GroupStatus status1, List<String> menteeIds, GroupStatus status2);

    Page<Group> findAllByNameLikeIgnoreCaseAndMentorsIn(
            String name, String mentorId, Pageable pageable);

    Page<Group> findAllByNameLikeIgnoreCaseAndMenteesIn(
            String name, String menteeId, Pageable pageable);

    Page<Group> findAllByMentorsInAndMenteesIn(String mentorId, String menteeId, Pageable pageable);

    Page<Group> findAllByNameLikeIgnoreCaseAndMentorsInAndMenteesIn(
            String name, String mentorId, String menteeId, Pageable pageable);

    boolean existsByMenteesIn(List<String> menteeIds);

    List<Group> findByIdIn(List<String> ids);

    List<Group> findAllByGroupCategory(String groupCategoryId);

    List<Group> findAllByGroupCategoryIn(List<String> groupCategoryIds);

    List<Group> findAllByGroupCategoryAndCreatorId(String groupCategoryIds, String creatorId);

    List<Group> findAllById(List<String> ids);

    @Aggregation(
            pipeline = {
                    "{$match: {$expr : {$eq: ['$_id' , {$toObjectId: ?0}]}}}",
                    "{$addFields: {groupCategoryObjectId: {$toObjectId: '$groupCategory'}}}",
                    "{$lookup: {from: 'group_category', localField: 'groupCategoryObjectId', foreignField: '_id', as: 'category'}}",
                    "{$unwind: '$category'}",
                    "{$set: {groupCategory: '$category.name'}}",
                    "{$unset: 'category'}",
                    "{$unset: 'groupCategoryObjectId'}"
            })
    List<GroupDetailResponse> getGroupDetail(String groupId);



    long countByStatus(GroupStatus status);

    long countByGroupCategoryAndStatus(String groupCategoryId, GroupStatus status);

    long countByGroupCategoryAndStatusAndCreatorId(
            String groupCategoryId, GroupStatus status, String creatorId);

    long countByCreatedDateBetween(Date start, Date end);

    long countByCreatedDateBetweenAndCreatorId(Date start, Date end, String creatorId);

    long countByGroupCategoryAndCreatedDateBetween(String groupCategoryId, Date start, Date end);

    boolean existsByIdAndCreatorId(String groupId, String creatorId);

    Page<Group> findAllByCreatorId(Pageable pageable, String creatorId);

    long countByCreatorId(String creatorId);

    long countByStatusAndCreatorId(GroupStatus status, String creatorId);

    List<Group> findAllByCreatorId(String creatorId);

    List<Group> findAllByCreatorIdOrderByCreatedDate(String creatorId);
    List<Group> findByMenteesContainsOrMentorsContains(String menteeId, String mentorId);

    List<Group> findByMenteesContainsOrMentorsContainsAndStatusIs(String menteeId, String mentorId, GroupStatus status);
}
