package vn.edu.hcmus.mentor.backend.repository.custom;

import vn.edu.hcmus.mentor.backend.domain.Note;

import java.util.List;
import java.util.Optional;

public interface NoteCustomRepository {
    Long countNotesByUserId(String userId);

    List<Note> getNotes();

    Optional<Note> getNoteById(String noteId);
}