package vn.edu.hcmus.mentor.backend.repository.custom;

import vn.edu.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserCustomRepository {
    Page<NoteUserProfile> findAllAccessNote(String viewerId, String query, Pageable pageable);
}