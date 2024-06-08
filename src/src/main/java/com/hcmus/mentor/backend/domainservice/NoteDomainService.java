package com.hcmus.mentor.backend.domainservice;

import com.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;
import com.hcmus.mentor.backend.repository.NoteRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
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

    public Long countNotes() {
        return null;
    }

    public List<NoteUserProfile> getAllUsers(String userId) {
        return userRepository.findAllAccessNote();
    }
}