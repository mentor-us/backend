package com.hcmus.mentor.backend.repository.custom.impl;

import com.hcmus.mentor.backend.controller.usecase.schoolyear.search.SearchSchoolYearQuery;
import com.hcmus.mentor.backend.domain.SchoolYear;
import com.hcmus.mentor.backend.repository.custom.SchoolYearRepositoryCustom;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Optional;

import static com.hcmus.mentor.backend.domain.QSchoolYear.schoolYear;

public class SchoolYearRepositoryImpl extends QuerydslRepositorySupport implements SchoolYearRepositoryCustom {

    private final EntityManager em;

    public SchoolYearRepositoryImpl(EntityManager em) {
        super(SchoolYear.class);
        this.em = em;
    }

    @Override
    public Page<SchoolYear> search(SearchSchoolYearQuery query) {
        var statement = searchBase(schoolYear, query);

        // Paging
        var pageable = PageRequest.of(query.getPage(), query.getPageSize());
        Optional.ofNullable(getQuerydsl()).ifPresent(querydsl -> querydsl.applyPagination(pageable, statement));

        var rows = statement.fetch();
        var total = searchCount(schoolYear.count(), query).fetchOne();

        return PageableExecutionUtils.getPage(rows, pageable, () -> Optional.ofNullable(total).orElse(0L));
    }

    private <T> JPAQuery<T> searchBase(Expression<T> expression, SearchSchoolYearQuery query) {
        var statement = new JPAQuery<SchoolYear>(em)
                .select(expression)
                .from(schoolYear);

        // Filter

        // Sorting
        statement.orderBy(schoolYear.createdDate.asc());

        return statement;
    }

    private <T> JPAQuery<T> searchCount(Expression<T> expression, SearchSchoolYearQuery query) {
        var statement = new JPAQuery<SchoolYear>(em)
                .select(expression)
                .from(schoolYear);

        // Filter

        return statement;
    }
}
