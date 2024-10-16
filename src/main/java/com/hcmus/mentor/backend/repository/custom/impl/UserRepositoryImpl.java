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
    public Page<NoteUserProfile> findAllAccessNote(String viewerId, String query, Pageable pageable) {
        Long totalCount = findAllUsersCanBeAssessQuery(user.count(), viewerId, query).fetchOne();

        var noteUserProfileProjection = Projections.bean(NoteUserProfile.class,
                user.id, user.name, user.email, user.imageUrl, note.id.countDistinct().as("totalNotes"));
        JPAQuery<NoteUserProfile> usersQuery = findAllUsersCanBeAssessQuery(noteUserProfileProjection, viewerId, query)
                .groupBy(user.id, user.name, user.email, user.imageUrl);
        Optional.ofNullable(getQuerydsl()).ifPresent(querydsl -> querydsl.applyPagination(pageable, usersQuery));

        return PageableExecutionUtils.getPage(usersQuery.fetch(), pageable, () -> Optional.ofNullable(totalCount).orElse(0L));
    }

    private <T> JPAQuery<T> findAllUsersCanBeAssessQuery(Expression<T> expression, String viewerId, String query) {
        QUser u = QUser.user;
        QNote n = QNote.note;
        var querySql = new JPAQuery<>(em)
                .select(expression)
                .from(u)
                .leftJoin(n).on(n.creator.id.eq(viewerId)
                        .or(n.owner.id.eq(viewerId))
                        .or(n.userAccesses.any().user.id.eq(viewerId)))
                .where(n.users.any().id.eq(u.id));
        if (query != null && !query.isEmpty()) {
            querySql.where(u.name.containsIgnoreCase(query).or(u.email.containsIgnoreCase(query)));
        }

        return querySql;
    }

}