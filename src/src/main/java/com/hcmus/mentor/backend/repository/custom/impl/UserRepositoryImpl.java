package com.hcmus.mentor.backend.repository.custom.impl;

import com.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;
import com.hcmus.mentor.backend.domain.QNote;
import com.hcmus.mentor.backend.domain.QUser;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.custom.UserCustomRepository;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Optional;

import static com.hcmus.mentor.backend.domain.QNote.note;
import static com.hcmus.mentor.backend.domain.QUser.user;

public class UserRepositoryImpl extends QuerydslRepositorySupport implements UserCustomRepository {

    private final EntityManager em;

    public UserRepositoryImpl(EntityManager em) {
        super(User.class);
        this.em = em;
    }

    @Override
    public Page<NoteUserProfile> findAllAccessNote(String viewerId, Pageable pageable) {
        Long totalCount = findAllUsersCanBeAssessQuery(user.count(), viewerId).fetchOne();
        JPAQuery<NoteUserProfile> query = findAllUsersCanBeAssessQuery(Projections.bean(NoteUserProfile.class, user.id, user.name, user.email, user.imageUrl, note.id.countDistinct().as("totalNotes")), viewerId)
                .groupBy(user.id, user.name, user.email, user.imageUrl);
        Optional.ofNullable(getQuerydsl()).ifPresent(querydsl -> querydsl.applyPagination(pageable, query));
        return PageableExecutionUtils.getPage(query.fetch(), pageable, () -> Optional.ofNullable(totalCount).orElse(0L));
    }

    private <T> JPAQuery<T> findAllUsersCanBeAssessQuery(Expression<T> expression, String viewerId) {
        QUser u = QUser.user;
        QNote n = QNote.note;

        return new JPAQuery<>(em)
                .select(expression)
                .from(u)
                .leftJoin(n).on(n.creator.id.eq(viewerId)
                        .or(n.owner.id.eq(viewerId))
                        .or(n.userAccesses.any().user.id.eq(viewerId)))
                .where(n.users.any().id.eq(u.id));
    }
}