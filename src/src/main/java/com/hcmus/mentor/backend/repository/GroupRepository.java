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
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String>, JpaSpecificationExecutor<Group> {

    long countByStatus(GroupStatus status);

    long countByGroupCategoryIdAndStatus(String groupCategoryId, GroupStatus status);

    long countByGroupCategoryIdAndStatusAndCreatorId(String groupCategoryId, GroupStatus status, String creatorId);

    long countByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

    long countByCreatedDateBetweenAndCreatorId(LocalDateTime start, LocalDateTime end, String creatorId);

    long countByGroupCategoryIdAndCreatedDateBetween(String groupCategoryId, LocalDateTime start, LocalDateTime end);

    long countByStatusAndCreatorId(GroupStatus status, String creatorId);

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
            "JOIN g.groupUsers gu " +
            "WHERE gu.user.id = ?1 " +
            "AND gu.isMentor = false " +
            "AND g.status = 'ACTIVE'")
    List<Group> findAllByMenteesIn(String menteeId);

    @Query("SELECT g, gu " +
            "FROM Group g " +
            "INNER JOIN FETCH g.groupUsers gu  " +
            "WHERE gu.user.id = ?1 " +
            "AND gu.isMentor = true " +
            "AND g.status = 'ACTIVE'")
    List<Group> findAllByMentorsIn(String mentorId);

    @Query("SELECT g " +
            "FROM Group g " +
            "JOIN g.groupUsers gu  " +
            "WHERE gu.user.id = ?1 " +
            "AND g.status = 'ACTIVE'")
    Page<Group> findAllByIsMember(String memberId, Pageable pageable);

    @Query("SELECT g " +
            "FROM Group g " +
            "JOIN g.groupUsers gu " +
            "WHERE gu.user.id = ?1 " +
            "AND g.status = ?2")
    Slice<Group> findByIsMemberAndStatus(String userId, GroupStatus status, Pageable pageable);

    @Query("SELECT g, gu " +
            "FROM Group g " +
            "JOIN g.groupUsers gu " +
            "WHERE gu.user.id = ?1 " +
            "AND g.status = ?2")
    List<Group> findByIsMemberAndStatus(String userId, GroupStatus status);

    @Query("SELECT g " +
            "FROM Group g " +
            "JOIN g.groupUsers gu " +
            "WHERE gu.user.id = ?1")
    List<Group> findGroupsByMembersContaining(String userId);
}