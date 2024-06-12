package com.hcmus.mentor.backend.repository.custom.impl;

import com.hcmus.mentor.backend.controller.usecase.course.search.SearchCourseQuery;
import com.hcmus.mentor.backend.domain.Course;
import com.hcmus.mentor.backend.repository.custom.CourseRepositoryCustom;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

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
        var statement = searchBase(course, query);

        // Paging
        var pageable = PageRequest.of(query.getPage(), query.getPageSize());
        Optional.ofNullable(getQuerydsl()).ifPresent(querydsl -> querydsl.applyPagination(pageable, statement));

        var rows = statement.fetch();
        var total = searchCount(course.count(), query).fetchOne();

        return PageableExecutionUtils.getPage(rows, pageable, () -> Optional.ofNullable(total).orElse(0L));
    }

    private <T> JPAQuery<T> searchBase(Expression<T> expression, SearchCourseQuery query) {
        var statement = new JPAQuery<Course>(em)
                .select(expression)
                .from(course);

        // Filter

        // Sorting
        statement.orderBy(course.createdDate.asc());

        return statement;
    }

    private <T> JPAQuery<T> searchCount(Expression<T> expression, SearchCourseQuery query) {
        var statement = new JPAQuery<Course>(em)
                .select(expression)
                .from(course);

        // Filter

        return statement;
    }
}
