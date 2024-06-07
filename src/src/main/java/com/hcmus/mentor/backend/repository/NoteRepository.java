package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {
}