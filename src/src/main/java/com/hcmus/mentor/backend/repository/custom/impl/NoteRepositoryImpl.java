package com.hcmus.mentor.backend.repository.custom.impl;

import com.hcmus.mentor.backend.domain.Note;
import com.hcmus.mentor.backend.repository.custom.NoteCustomRepository;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import static com.hcmus.mentor.backend.domain.QNote.note;
import static com.hcmus.mentor.backend.domain.QUser.user;

public class NoteRepositoryImpl extends QuerydslRepositorySupport implements NoteCustomRepository {

    private final EntityManager em;

    public NoteRepositoryImpl(EntityManager em) {
        super(Note.class);
        this.em = em;
    }

    @Override
    public Long countNotesByUserId(String userId) {
        return new JPAQuery<Note>(em)
                .select(note.count())
                .from(note)
                .join(note.users, user)
                .where(user.id.eq(userId))
                .fetchOne();

    }

    private Long countNotesByUserIds(String userId) {
        return new JPAQuery<Note>(em)
                .select(note.count())
                .from(note)
                .join(note.users, user)
                .where(user.id.eq(userId))
                .fetchOne();
    }
}