package com.hcmus.mentor.backend.repository.custom.impl;

import com.google.common.base.Strings;
import com.hcmus.mentor.backend.controller.usecase.grade.getgrade.SearchGradeQuery;
import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.domain.QUser;
import com.hcmus.mentor.backend.repository.custom.GradeRepositoryCustom;
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

import static com.hcmus.mentor.backend.domain.QGrade.grade;

public class GradeRepositoryImpl extends QuerydslRepositorySupport implements GradeRepositoryCustom {

    private final EntityManager em;

    public GradeRepositoryImpl(EntityManager em) {
        super(Grade.class);
        this.em = em;
    }

    @Override
    public Page<Grade> search(SearchGradeQuery query) {
        var statement = searchResult(grade, query);

        // Paging
        var pageable = PageRequest.of(query.getPage(), query.getPageSize());
        Optional.ofNullable(getQuerydsl()).ifPresent(querydsl -> querydsl.applyPagination(pageable, statement));

        var rows = statement.fetch();
        var total = searchCount(grade.count(), query).fetchOne();

        return PageableExecutionUtils.getPage(rows, pageable, () -> Optional.ofNullable(total).orElse(0L));
    }

    private <T> JPAQuery<T> searchResult(Expression<T> expression, SearchGradeQuery query) {
        var student = new QUser("student");
        var statement = new JPAQuery<Grade>(em)
                .select(expression)
                .from(grade)
                .join(grade.student, student).fetchJoin()
                .join(grade.creator, new QUser("creator")).fetchJoin();

        // Filter
        applyFilter(query, statement, student);

        // Sorting
        if (!Strings.isNullOrEmpty(query.getOrderBy())) {
            OrderUtil.addOrderBy(
                    statement,
                    OrderUtil.parseSeparated(query.getOrderBy()),
                    List.of(MutablePair.of("create", grade.createdDate),
                            MutablePair.of("update", grade.updatedDate),
                            MutablePair.of("year", grade.year),
                            MutablePair.of("semester", grade.semester),
                            MutablePair.of("courseName", grade.courseName),
                            MutablePair.of("courseCode", grade.courseCode),
                            MutablePair.of("isRetake", grade.isRetake),
                            MutablePair.of("grade", grade.score),
                            MutablePair.of("studentId", student.id))
            );
        } else {
            statement.orderBy(grade.createdDate.desc());
        }

        return statement;
    }

    private <T> JPAQuery<T> searchCount(Expression<T> expression, SearchGradeQuery query) {
        var student = new QUser("student");
        var statement = new JPAQuery<Grade>(em)
                .select(expression)
                .from(grade)
                .join(grade.student, student)
                .join(grade.creator, new QUser("creator"));

        // Filter
        applyFilter(query, statement, student);

        return statement;
    }

    private static <T> void applyFilter(SearchGradeQuery query, JPAQuery<T> statement, QUser student) {
        if (!Strings.isNullOrEmpty(query.getUserId())) {
            statement.where(student.id.eq(query.getUserId()));
        }
        if (!Strings.isNullOrEmpty(query.getCourseName())) {
            statement.where(grade.courseName.eq(query.getCourseName()));
        }
        if (!Strings.isNullOrEmpty(query.getCourseCode())) {
            statement.where(grade.courseCode.eq(query.getCourseCode()));
        }
        if (!Strings.isNullOrEmpty(query.getYear())) {
            statement.where(grade.year.eq(query.getYear()));
        }
        Optional.ofNullable(query.getSemester()).ifPresent(semester -> statement.where(grade.semester.eq(semester)));
        Optional.ofNullable(query.getIsRetake()).ifPresent(isRetake -> statement.where(grade.isRetake.eq(isRetake)));
    }
}