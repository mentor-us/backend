package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import com.hcmus.mentor.backend.repository.custom.UserCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User>, UserCustomRepository {

    long countByStatus(Boolean status);

//    long countByIdInAndStatus(List<String> userIds, Boolean status);

    long countByCreatedDateBetween(Date start, Date end);

//    long countByIdInAndCreatedDateBetween(List<String> userIds, Date start, Date end);

    Boolean existsByEmail(String email);

    Boolean existsByEmailAndRolesContains(String email, UserRole role);

    Boolean existsByIdAndRolesContains(String id, UserRole role);

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT ?2 in " +
            "(SELECT gu1.user_id " +
            "FROM group_user gu1 " +
            "JOIN ( SELECT DISTINCT gu.group_id " +
            "       FROM group_user gu WHERE gu.user_id = ?1 ) AS abc ON abc.group_id = gu1.group_id " +
            "WHERE gu1.is_mentor = true)", nativeQuery = true)
    Boolean isMentorOfUser(String userId, String mentorId);

    Page<User> findByEmailLikeIgnoreCase(String email, Pageable pageable);

    List<User> findByEmailLikeIgnoreCase(String email);

    List<User> findByIdIn(List<String> ids);

    List<User> findAllByIdIn(List<String> ids);

//    List<User> findAllByRolesIn(List<String> rolesIds);

    @org.jetbrains.annotations.NotNull
    Optional<User> findById(String id);

    @Query("SELECT u FROM User u WHERE u.email = ?1 OR ?1 MEMBER OF u.additionalEmails")
    Optional<User> findByAdditionalEmailsContains(String email);

    @Query("SELECT u FROM User u WHERE u.id = ?1")
    Optional<User> findShortProfile(String id);
}