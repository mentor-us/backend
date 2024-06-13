package com.hcmus.mentor.backend.repository.custom.impl;

import com.hcmus.mentor.backend.controller.usecase.semester.search.SearchSemesterQuery;
import com.hcmus.mentor.backend.domain.Semester;
import com.hcmus.mentor.backend.repository.custom.SemesterRepositoryCustom;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Optional;

import static com.hcmus.mentor.backend.domain.QSemester.semester;

public class SemesterRepositoryImpl extends QuerydslRepositorySupport implements SemesterRepositoryCustom {

    private final EntityManager em;

    public SemesterRepositoryImpl(EntityManager em) {
        super(Semester.class);
        this.em = em;
    }

    @Override
    public Page<Semester> search(SearchSemesterQuery query) {
        var statement = searchBase(semester, query);

        // Paging
        var pageable = PageRequest.of(query.getPage(), query.getPageSize());
        Optional.ofNullable(getQuerydsl()).ifPresent(querydsl -> querydsl.applyPagination(pageable, statement));

        var rows = statement.fetch();
        var total = searchCount(semester.count(), query).fetchOne();

        return PageableExecutionUtils.getPage(rows, pageable, () -> Optional.ofNullable(total).orElse(0L));
    }

    private <T> JPAQuery<T> searchBase(Expression<T> expression, SearchSemesterQuery query) {
        var statement = new JPAQuery<Semester>(em)
                .select(expression)
                .from(semester);

        // Filter

        // Sorting
        statement.orderBy(semester.createdDate.asc());

        return statement;
    }

    private <T> JPAQuery<T> searchCount(Expression<T> expression, SearchSemesterQuery query) {
        var statement = new JPAQuery<Semester>(em)
                .select(expression)
                .from(semester);

        // Filter

        return statement;
    }
}
