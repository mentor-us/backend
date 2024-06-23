package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.controller.usecase.note.common.NoteEditableProjection;
import com.hcmus.mentor.backend.domain.Note;
import com.hcmus.mentor.backend.repository.custom.NoteCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, String>, NoteCustomRepository {

    @Query(value = "SELECT DISTINCT n.* " +
            "FROM " +
            "    notes n " +
            "    JOIN ref_user_note nu ON nu.note_id = n.id AND nu.user_id = ?1 " +
            "    LEFT JOIN note_user_access nua ON nua.note_id = n.id " +
            "    LEFT JOIN list_mentors mt ON mt.mentee_id = nu.user_id " +
            "WHERE " +
            "    n.share_type = 'PUBLIC'  " +
            "    OR (n.share_type IN ('MENTOR_VIEW', 'MENTOR_EDIT') AND mt.mentor_id = ?2)  " +
            "    OR n.creator_id = ?2  " +
            "    OR n.owner_id = ?2  " +
            "    OR nua.user_id = ?2 " +
            "GROUP BY n.id " +
            "ORDER BY n.created_date DESC", nativeQuery = true)
    Page<Note> findAllByUserIdWithViewerId(String userId, String viewerId, Pageable pageable);

    @Query(value = "SELECT " +
            "    n.id, " +
            "    MAX( " +
            "        CASE  " +
            "            WHEN n.owner_id = ?2  " +
            "                 OR (n.share_type = 'MENTOR_EDIT' AND mt.mentor_id = ?2)  " +
            "                 OR (nua.note_permission = 'EDIT' AND nua.user_id = ?2) " +
            "            THEN 1  " +
            "            ELSE 0  " +
            "        END " +
            "    ) AS canEdit " +
            "FROM  " +
            "    notes n " +
            "    JOIN ref_user_note nu ON nu.note_id = n.id AND nu.user_id = ?1 " +
            "    LEFT JOIN note_user_access nua ON nua.note_id = n.id " +
            "    LEFT JOIN list_mentors mt ON mt.mentee_id = nu.user_id " +
            "WHERE " +
            "    n.share_type = 'PUBLIC'  " +
            "    OR (n.share_type IN ('MENTOR_VIEW', 'MENTOR_EDIT') AND mt.mentor_id = ?2)  " +
            "    OR n.creator_id = ?2  " +
            "    OR n.owner_id = ?2  " +
            "    OR nua.user_id = ?2 " +
            "GROUP BY n.id", nativeQuery = true)
    List<NoteEditableProjection> findAllByUserIdWithViewerIdCanEdit(String userId, String viewerId);

    @Query(value = "SELECt exists" +
            "(SELECT n.id " +
            "FROM " +
            "    notes n " +
            "    JOIN ref_user_note nu ON nu.note_id = n.id " +
            "    LEFT JOIN note_user_access nua ON nua.note_id = n.id " +
            "    LEFT JOIN list_mentors mt ON mt.mentee_id = nu.user_id " +
            "WHERE " +
            "n.id = ?1 " +
            "AND (n.owner_id = ?2  " +
            "    OR ((n.share_type = 'MENTOR_EDIT') AND mt.mentor_id = ?2)  " +
            "    OR (nua.user_id = ?2 AND nua.note_permission = 'EDIT')))", nativeQuery = true)
    boolean canEdit(String noteId, String editor);

    @Query(value = "SELECt exists" +
            "(SELECT n.id " +
            "FROM " +
            "    notes n " +
            "    JOIN ref_user_note nu ON nu.note_id = n.id " +
            "    LEFT JOIN note_user_access nua ON nua.note_id = n.id " +
            "    LEFT JOIN list_mentors mt ON mt.mentee_id = nu.user_id " +
            "WHERE " +
            "   n.id = ?1 " +
            "   AND (n.share_type = 'PUBLIC'  " +
            "       OR (n.share_type IN ('MENTOR_VIEW', 'MENTOR_EDIT') AND mt.mentor_id = ?2) " +
            "       OR n.creator_id = ?2  " +
            "       OR n.owner_id = ?2  " +
            "       OR nua.user_id = ?2)) ", nativeQuery = true)
    boolean canView(String noteId, String viewer);
}