package com.hcmus.mentor.backend.repository.custom;

import com.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;

import java.util.List;

public interface UserCustomRepository {
    List<NoteUserProfile> findAllAccessNote();
}