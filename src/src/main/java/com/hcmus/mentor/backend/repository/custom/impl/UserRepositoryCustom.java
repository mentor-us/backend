package com.hcmus.mentor.backend.repository.custom.impl;

import com.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;

import java.util.List;

public interface UserRepositoryCustom {
    List<NoteUserProfile> findAllAccessNote();
}