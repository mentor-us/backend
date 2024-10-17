package vn.edu.hcmus.mentor.backend.repository.custom.impl;

import vn.edu.hcmus.mentor.backend.domain.Note;
import vn.edu.hcmus.mentor.backend.domain.QUser;
import vn.edu.hcmus.mentor.backend.repository.custom.NoteCustomRepository;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Optional;

import static vn.edu.hcmus.mentor.backend.domain.QNote.note;
import static vn.edu.hcmus.mentor.backend.domain.QNoteHistory.noteHistory;
import static vn.edu.hcmus.mentor.backend.domain.QNoteUserAccess.noteUserAccess;
import static vn.edu.hcmus.mentor.backend.domain.QUser.user;

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

    @Override
    public List<Note> getNotes() {
        return new JPAQuery<Note>(em)
                .select(note)
                .from(note)
                .join(note.creator, user).fetchJoin()
                .leftJoin(note.updatedBy, new QUser("updatedBy")).fetchJoin()
                .leftJoin(note.noteHistories, noteHistory).fetchJoin()
                .fetch();
    }

    @Override
    public Optional<Note> getNoteById(String noteId) {
        return Optional.ofNullable(new JPAQuery<Note>(em)
                .select(note)
                .from(note)
                .join(note.creator, new QUser("creator")).fetchJoin()
                .leftJoin(note.updatedBy, new QUser("updatedBy")).fetchJoin()
                .leftJoin(note.users, new QUser("users")).fetchJoin()
                .leftJoin(note.noteHistories, noteHistory).fetchJoin()
                .leftJoin(note.userAccesses, noteUserAccess).fetchJoin()
                .where(note.id.eq(noteId))
                .fetchOne());
    }

    private Long countNotesByUserIds(String userId) {
        return new JPAQuery<Note>(em)
                .select(note.count())
                .from(note)
                .join(note.creator, user).fetchJoin()
                .join(note.updatedBy, new QUser("updatedBy")).fetchJoin()
                .where(user.id.eq(userId))
                .fetchOne();
    }
}