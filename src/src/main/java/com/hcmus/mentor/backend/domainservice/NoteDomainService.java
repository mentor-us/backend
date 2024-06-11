package com.hcmus.mentor.backend.domainservice;

import com.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;
import com.hcmus.mentor.backend.repository.NoteRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NoteDomainService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    public Long countNotes() {
        return null;
    }

    public List<NoteUserProfile> getAllUsers() {
        return userRepository.findAllAccessNote();
    }
}