package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String>, JpaSpecificationExecutor<Group> {

    long countByStatus(GroupStatus status);

    long countByGroupCategoryIdAndStatus(String groupCategoryId, GroupStatus status);

    long countByGroupCategoryIdAndStatusAndCreatorId(String groupCategoryId, GroupStatus status, String creatorId);

    long countByCreatedDateBetween(Date start, Date end);

    long countByCreatedDateBetweenAndCreatorId(Date start, Date end, String creatorId);

    long countByGroupCategoryIdAndCreatedDateBetween(String groupCategoryId, Date start, Date end);

    long countByStatusAndCreatorId(GroupStatus status, String creatorId);

    boolean existsByIdAndCreatorId(String groupId, String creatorId);

    boolean existsByName(String s);

    boolean existsByCreatorEmailAndId(String creatorEmail, String groupId);

    @NotNull
    Optional<Group> findById(@NotNull String id);

    List<Group> findAllByOrderByCreatedDate();

    List<Group> findAllByGroupCategoryId(String groupCategoryId);

    List<Group> findAllByGroupCategoryIdIn(List<String> groupCategoryIds);

    List<Group> findAllByGroupCategoryIdAndCreatorId(String groupCategoryIds, String creatorId);

    Page<Group> findAllByCreatorId(Pageable pageable, String creatorId);

    List<Group> findAllByCreatorId(String creatorId);

    List<Group> findAllByCreatorIdOrderByCreatedDate(String creatorId);

    List<Group> findByIdIn(List<String> ids);

    @Query("SELECT g, gu " +
            "FROM Group g " +
            "INNER JOIN FETCH g.groupUsers gu " +
            "WHERE gu.user.id = ?1 " +
            "AND gu.isMentor = false")
    List<Group> findAllByMenteesIn(String menteeId);

    @Query("SELECT g, gu " +
            "FROM Group g " +
            "INNER JOIN FETCH g.groupUsers gu  " +
            "WHERE gu.user.id = ?1 " +
            "AND gu.isMentor = true")
    List<Group> findAllByMentorsIn(String mentorId);

    @Query("SELECT g, gu " +
            "FROM Group g " +
            "INNER JOIN FETCH g.groupUsers gu  " +
            "WHERE gu.user.id = ?1")
    Page<Group> findAllByIsMember(String memberId, Pageable pageable);

    @Query("SELECT g " +
            "FROM Group g " +
            "INNER JOIN FETCH g.groupUsers gu " +
            "INNER JOIN FETCH gu.user u " +
            "WHERE gu.user.id = ?1 " +
            "AND g.status = ?2")
    Slice<Group> findByIsMemberAndStatus(String userId, GroupStatus status, Pageable pageable);

    @Query("SELECT g, gu " +
            "FROM Group g " +
            "INNER JOIN FETCH g.groupUsers gu " +
            "WHERE gu.user.id = ?1 " +
            "AND g.status = ?2")
    List<Group> findByIsMemberAndStatus(String userId, GroupStatus status);

    @Query("SELECT g , gu, gc " +
            "FROM Group g " +
            "INNER JOIN FETCH g.groupCategory gc " +
            "INNER JOIN FETCH g.groupUsers gu " +
            "WHERE g.id = ?1")
    Optional<Group> findByIdAndFetchGroupCategoryAndFetch(String id);


    @Query("SELECT g " +
            "FROM Group g " +
            "INNER JOIN FETCH g.groupUsers " +
            "WHERE g.status = :status " +
            "ORDER BY g.createdDate DESC")
    List<Group> findAllByStatusAnd(@Param("status") GroupStatus status);

    @Query("SELECT g, gu, c " +
            "FROM Group g  " +
            "INNER JOIN FETCH g.groupUsers gu  " +
            "INNER JOIN FETCH g.channels c  " +
            "WHERE g.status = :status " +
            "AND gu.user.id = :userId")
    List<Group> fetchWithUsersAndChannels(@Param("userId") String userId, @Param("status") GroupStatus status);

    @Query("SELECT g, gu, u " +
            "from Group g " +
            "INNER JOIN FETCH g.groupUsers gu " +
            "INNER JOIN FETCH gu.user u " +
            "where u.id = :userId")
    List<Group> findGroupsByMembersContaining(String userId);

    @Query("SELECT g from Group g join g.groupUsers gu join gu.user u where u.id = :userId and g.id = :groupId")
    boolean existsByMember(@Param("groupId") String groupId, @Param("userId") String userId);
}