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
            "FROM notes n " +
            "JOIN users u ON n.creator_id = u.\"id\" OR n.owner_id = u.\"id\" " +
            "LEFT JOIN note_user_access nua ON nua.note_id = n.\"id\" " +
            "LEFT JOIN ref_user_note nu ON nu.note_id = n.\"id\" " +
            "WHERE ( u.\"id\" = ?2 OR nua.user_id = ?2 ) AND nu.user_id = ?1 " +
            "ORDER BY n.created_date DESC", nativeQuery = true)
    Page<Note> findAllByUserIdWithViewerId(String userId, String viewerId, Pageable pageable);

    @Query(value = "SELECT n.id, " +
            "MAX(CASE WHEN n.owner_id = ?2 OR (nua.user_id = ?2 AND nua.note_Permission = 'EDIT') THEN 1 ELSE 0 END) AS canEdit " +
            "FROM notes n " +
            "JOIN users u ON n.creator_id = u.id OR n.owner_id = u.id " +
            "LEFT JOIN note_user_access nua ON nua.note_id = n.id " +
            "JOIN ref_user_note nu ON nu.note_id = n.id AND nu.user_id = ?1 " +
            "WHERE u.id = ?2 OR nua.user_id = ?2 " +
            "GROUP BY n.id, n.created_date " +
            "ORDER BY n.created_date DESC", nativeQuery = true)
    List<NoteEditableProjection> findAllByUserIdWithViewerIdCanEdit(String userId, String viewerId);

    @Query(value = "SELECt exists" +
            "(SELECT n.id FROM notes n " +
            "LEFT JOIN users u ON n.owner_id = u.id " +
            "LEFT JOIN note_user_access nua ON n.id = nua.note_id " +
            "WHERE (u.id = ?2 OR (nua.user_id = ?2 AND nua.note_Permission = 'EDIT')) AND n.id = ?1) ", nativeQuery = true)
    boolean canEdit(String noteId, String editor);

    @Query(value = "SELECt exists" +
            "( SELECT n.id FROM notes n " +
            "LEFT JOIN users u ON n.owner_id = u.id " +
            "LEFT JOIN note_user_access nua ON n.id = nua.note_id " +
            "WHERE (u.id = ?2 OR nua.user_id = ?2) AND n.id = ?1) ", nativeQuery = true)
    boolean canView(String noteId, String viewer);
}