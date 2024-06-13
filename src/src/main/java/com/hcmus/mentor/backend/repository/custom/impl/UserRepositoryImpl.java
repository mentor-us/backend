package com.hcmus.mentor.backend.repository.custom.impl;

import com.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;
import com.hcmus.mentor.backend.domain.QUser;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.custom.UserCustomRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.hcmus.mentor.backend.domain.QNote.note;
import static com.hcmus.mentor.backend.domain.QNoteUserAccess.noteUserAccess;
import static com.hcmus.mentor.backend.domain.QUser.user;

public class UserRepositoryImpl extends QuerydslRepositorySupport implements UserCustomRepository {

    private final EntityManager em;

    public UserRepositoryImpl(EntityManager em) {
        super(User.class);
        this.em = em;
    }

    @Override
    public List<NoteUserProfile> findAllAccessNote() {
        var abc = new QUser(user);
        return new JPAQuery<NoteUserProfile>(em)
                .select(Projections.bean(NoteUserProfile.class, user.id, user.name, user.email, user.imageUrl, note.countDistinct().as("totalNotes")))
                .from(user, note)
                .leftJoin(user.noteUserAccesses, noteUserAccess)
                .where(
                        note.creator.id.eq(user.id)
                                .or(note.owner.id.eq(user.id))
                                .or(noteUserAccess.note.id.eq(note.id).and(noteUserAccess.user.id.eq(user.id)))
                )
                .groupBy(user.id, user.name, user.email, user.imageUrl)
                .distinct()
                .fetch();
    }
}