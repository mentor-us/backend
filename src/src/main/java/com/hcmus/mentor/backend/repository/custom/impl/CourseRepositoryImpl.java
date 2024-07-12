package com.hcmus.mentor.backend.repository.custom.impl;

import com.google.common.base.Strings;
import com.hcmus.mentor.backend.controller.usecase.course.search.SearchCourseQuery;
import com.hcmus.mentor.backend.domain.Course;
import com.hcmus.mentor.backend.repository.custom.CourseRepositoryCustom;
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

public class CourseRepositoryImpl extends QuerydslRepositorySupport implements CourseRepositoryCustom {

    private final EntityManager em;

    public CourseRepositoryImpl(EntityManager em) {
        super(Course.class);
        this.em = em;
    }

    @Override
    public Page<Course> search(SearchCourseQuery query) {
        var statement = searchResult(course, query);

        // Paging
        var pageable = PageRequest.of(query.getPage(), query.getPageSize());
        Optional.ofNullable(getQuerydsl()).ifPresent(querydsl -> querydsl.applyPagination(pageable, statement));

        var rows = statement.fetch();
        var total = searchCount(course.count(), query).fetchOne();

        return PageableExecutionUtils.getPage(rows, pageable, () -> Optional.ofNullable(total).orElse(0L));
    }

    private <T> JPAQuery<T> searchResult(Expression<T> expression, SearchCourseQuery query) {
        var statement = new JPAQuery<Course>(em)
                .select(expression)
                .from(course);

        // Filter
        applyFilter(query, statement);

        // Sorting
        if (!Strings.isNullOrEmpty(query.getOrderBy())) {
            OrderUtil.addOrderBy(
                    statement,
                    OrderUtil.parseSeparated(query.getOrderBy()),
                    List.of(MutablePair.of("name", course.name),
                            MutablePair.of("code", course.code),
                            MutablePair.of("create", course.createdDate)
                    )
            );
        } else {
            statement.orderBy(course.createdDate.asc());
        }

        return statement;
    }

    private <T> JPAQuery<T> searchCount(Expression<T> expression, SearchCourseQuery query) {
        var statement = new JPAQuery<Course>(em)
                .select(expression)
                .from(course);

        // Filter
        applyFilter(query, statement);

        return statement;
    }


    private static <T> void applyFilter(SearchCourseQuery query, JPAQuery<T> statement) {
        if (!Strings.isNullOrEmpty(query.getName())) {
            statement.where(course.name.containsIgnoreCase(query.getName()));
        }

        if (!Strings.isNullOrEmpty(query.getCode())) {
            statement.where(course.code.containsIgnoreCase(query.getCode()));
        }
    }
}
