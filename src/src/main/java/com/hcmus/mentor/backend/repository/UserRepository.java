package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfileProjection;
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

    @Query(value = "SELECT exists(SELECT * FROM list_mentors mt WHERE mt.mentor_id = ?2 AND mt.mentee_id = ?1)", nativeQuery = true)
    Boolean isMentorOfUser(String userId, String mentorId);

    @Query(value = "SELECT DISTINCT u.* " +
            "FROM users u " +
            "LEFT JOIN user_additional_emails e on e.user_id = u.id " +
            "LEFT JOIN user_roles r on r.user_id = u.id " +
            "LEFT JOIN list_mentors mt on u.id = mt.mentee_id " +
            "WHERE mt.mentor_id = ?1 " +
            "AND (?2 IS NULL OR UPPER ( u.email ) LIKE ?2 OR UPPER ( u.name ) LIKE ?2) ",
            nativeQuery = true)
    Page<User> findAllMenteeOfUserId(String userId, String query, Pageable pageable);

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

    @Query(
            value = "SELECT u.id, u.name, u.email, u.image_url, COUNT ( DISTINCT n.id  ) AS totalNotes " +
                    "FROM users u " +
                    "   JOIN ref_user_note nu ON nu.user_id = u.id  " +
                    "   JOIN notes n ON n.id  = nu.note_id " +
                    "   LEFT JOIN note_user_access nua ON nua.note_id = n.id  " +
                    "   LEFT JOIN list_mentors mt ON mt.mentee_id = u.id " +
                    "WHERE " +
                    "   (n.share_type = 'PUBLIC' " +
                    "       OR ( n.share_type IN ( 'MENTOR_VIEW', 'MENTOR_EDIT' ) AND mt.mentor_id = ?1 ) " +
                    "       OR n.creator_id =  ?1 " +
                    "       OR n.owner_id =  ?1 " +
                    "       OR nua.user_id =  ?1 )" +
                    "   AND (?2 is null " +
                    "       OR (UPPER(u.name) LIKE ?2 OR UPPER(u.email) LIKE ?2)) " +
                    "GROUP BY u.id, u.name, u.email, u.image_url ",
            countQuery = "SELECT COUNT ( * ) " +
                    "FROM users u " +
                    "   JOIN ref_user_note nu ON nu.user_id = u.id  " +
                    "   JOIN notes n ON n.id  = nu.note_id " +
                    "   LEFT JOIN note_user_access nua ON nua.note_id = n.id  " +
                    "   LEFT JOIN list_mentors mt ON mt.mentee_id = u.id " +
                    "WHERE " +
                    "   (n.share_type = 'PUBLIC' " +
                    "       OR ( n.share_type IN ( 'MENTOR_VIEW', 'MENTOR_EDIT' ) AND mt.mentor_id = ?1 ) " +
                    "       OR n.creator_id =  ?1 " +
                    "       OR n.owner_id =  ?1 " +
                    "       OR nua.user_id =  ?1 )" +
                    "   AND (?2 is null " +
                    "       OR (UPPER(u.name) LIKE ?2 OR UPPER(u.email) LIKE ?2)) " +
                    "GROUP BY u.id, u.name, u.email, u.image_url ",
            nativeQuery = true)
    Page<NoteUserProfileProjection> findAllUsersHasNoteAccess(String viewerId, String query, Pageable pageable);
}