package com.hcmus.mentor.backend.repository.custom;

public interface NoteCustomRepository {
    Long countNotesByUserId(String userId);
}