package com.hcmus.mentor.backend.repository.custom.impl;

import com.google.common.base.Strings;
import com.hcmus.mentor.backend.controller.usecase.semester.search.SearchSemesterQuery;
import com.hcmus.mentor.backend.domain.Semester;
import com.hcmus.mentor.backend.repository.custom.SemesterRepositoryCustom;
import com.hcmus.mentor.backend.util.OrderUtil;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

import static com.hcmus.mentor.backend.domain.QCourse.course;
import static com.hcmus.mentor.backend.domain.QSemester.semester;

public class SemesterRepositoryImpl extends QuerydslRepositorySupport implements SemesterRepositoryCustom {

    private final EntityManager em;

    public SemesterRepositoryImpl(EntityManager em) {
        super(Semester.class);
        this.em = em;
    }

    @Override
    public Page<Semester> search(SearchSemesterQuery query) {
        var statement = searchResult(semester, query);

        // Paging
        var pageable = PageRequest.of(query.getPage(), query.getPageSize());
        Optional.ofNullable(getQuerydsl()).ifPresent(querydsl -> querydsl.applyPagination(pageable, statement));

        var rows = statement.fetch();
        var total = searchCount(semester.count(), query).fetchOne();

        return PageableExecutionUtils.getPage(rows, pageable, () -> Optional.ofNullable(total).orElse(0L));
    }

    private <T> JPAQuery<T> searchResult(Expression<T> expression, SearchSemesterQuery query) {
        var statement = new JPAQuery<Semester>(em)
                .select(expression)
                .from(semester);

        // Filter
        applyFilter(query, statement);

        // Sorting
        if (!Strings.isNullOrEmpty(query.getOrderBy())) {
            OrderUtil.addOrderBy(
                    statement,
                    OrderUtil.parseSeparated(query.getOrderBy()),
                    List.of(MutablePair.of("name", semester.name),
                            MutablePair.of("create", semester.createdDate)
                    )
            );
        } else {
            statement.orderBy(semester.createdDate.asc());
        }

        return statement;
    }

    private <T> JPAQuery<T> searchCount(Expression<T> expression, SearchSemesterQuery query) {
        var statement = new JPAQuery<Semester>(em)
                .select(expression)
                .from(semester);

        // Filter
        applyFilter(query, statement);

        return statement;
    }

    private static <T> void applyFilter(SearchSemesterQuery query, JPAQuery<T> statement) {
        if (!Strings.isNullOrEmpty(query.getName())) {
            statement.where(semester.name.containsIgnoreCase(query.getName()));
        }
    }

}
