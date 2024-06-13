package com.hcmus.mentor.backend.repository.custom.impl;

import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.repository.custom.GroupRepositoryCustom;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

import static com.hcmus.mentor.backend.domain.QGroup.group;
import static com.hcmus.mentor.backend.domain.QGroupCategory.groupCategory;
import static com.hcmus.mentor.backend.domain.QUser.user;

/**
 * @author duov
 */
public class GroupRepositoryImpl extends QuerydslRepositorySupport implements GroupRepositoryCustom {

    private final EntityManager em;

    public GroupRepositoryImpl(EntityManager em) {
        super(Group.class);
        this.em = em;
    }

    @Override
    public Page<Group> findAllByCreatorId(Pageable pageable, String creatorId) {
        Long totalCount = findAllByCreatorIdQuery(group.count(), creatorId).fetchOne();
        JPAQuery<Group> query = findAllByCreatorIdQuery(group, creatorId);
        Optional.ofNullable(getQuerydsl()).ifPresent(querydsl -> querydsl.applyPagination(pageable, query));
        List<Group> pagedData = query.fetch();
        return PageableExecutionUtils.getPage(pagedData, pageable, () -> Optional.ofNullable(totalCount).orElse(0L));
    }

    @Override
    public List<Group> findAllByCreatorId(String creatorId) {
        JPAQuery<Group> groupJPAQuery = findAllByCreatorIdQuery(group, creatorId);
        return groupJPAQuery.fetch();
    }

    @Override
    public List<Group> findAllByCreatorIdOrderByCreatedDate(String creatorId) {
        JPAQuery<Group> groupJPAQuery = findAllByCreatorIdQuery(group, creatorId);
        return groupJPAQuery.orderBy(group.createdDate.asc()).fetch();
    }

    @Override
    public Page<Group> findAllWithPagination(Pageable pageable) {
        Long totalCount = countAllGroup();
        JPAQuery<Group> query = findAll();
        Optional.ofNullable(getQuerydsl()).ifPresent(querydsl -> querydsl.applyPagination(pageable, query));
        List<Group> pagedData = query.fetch();
        return PageableExecutionUtils.getPage(pagedData, pageable, () -> Optional.ofNullable(totalCount).orElse(0L));
    }

    private <T> JPAQuery<T> findAllByCreatorIdQuery(Expression<T> expression, String creatorId) {
        return new JPAQuery<Group>(em)
                .select(expression)
                .from(group)
                .leftJoin(group.groupCategory, groupCategory).fetchJoin()
                .leftJoin(group.creator, user).fetchJoin()
                .where(group.creator.id.eq(creatorId));
    }

    private JPAQuery<Group> findAll() {
        return new JPAQuery<Group>(em)
                .select(group)
                .from(group)
                .leftJoin(group.groupCategory, groupCategory).fetchJoin()
                .leftJoin(group.creator, user).fetchJoin();
    }

    private Long countAllGroup() {
        return new JPAQuery<Group>(em)
                .select(group.count())
                .from(group).fetchOne();
    }
}
