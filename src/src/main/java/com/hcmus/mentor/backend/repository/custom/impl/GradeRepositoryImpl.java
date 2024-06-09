package com.hcmus.mentor.backend.repository.custom.impl;

import com.google.common.base.Strings;
import com.hcmus.mentor.backend.controller.usecase.grade.getgrade.SearchGradeQuery;
import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.domain.QUser;
import com.hcmus.mentor.backend.repository.custom.GradeRepositoryCustom;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Optional;

import static com.hcmus.mentor.backend.domain.QCourse.course;
import static com.hcmus.mentor.backend.domain.QGrade.grade;
import static com.hcmus.mentor.backend.domain.QSchoolYear.schoolYear;
import static com.hcmus.mentor.backend.domain.QSemester.semester;

public class GradeRepositoryImpl extends QuerydslRepositorySupport implements GradeRepositoryCustom {

    private final EntityManager em;

    public GradeRepositoryImpl(EntityManager em) {
        super(Grade.class);
        this.em = em;
    }

    @Override
    public Page<Grade> searchGrade(SearchGradeQuery query) {
        var statement = searchGradeBase(grade, query);

        // Paging
        var pageable = PageRequest.of(query.getPage(), query.getPageSize());
        Optional.ofNullable(getQuerydsl()).ifPresent(querydsl -> querydsl.applyPagination(pageable, statement));

        var rows = statement.fetch();
        var total = searchGradeCount(grade.count(), query).fetchOne();

        return PageableExecutionUtils.getPage(rows, pageable, () -> Optional.ofNullable(total).orElse(0L));
    }

    private <T> JPAQuery<T> searchGradeBase(Expression<T> expression, SearchGradeQuery query) {
        var student = new QUser("student");
        var statement = new JPAQuery<Grade>(em)
                .select(expression)
                .from(grade)
                .join(grade.student, student).fetchJoin()
                .join(grade.creator, new QUser("creator")).fetchJoin()
                .join(grade.year, schoolYear).fetchJoin()
                .join(grade.semester, semester).fetchJoin()
                .join(grade.course, course).fetchJoin();

        // Filter
        if (!Strings.isNullOrEmpty(query.getUserId())) {
            statement.where(student.id.eq(query.getUserId()));
        }

        // Sorting
        statement.orderBy(grade.createdDate.desc());

        return statement;
    }

    private <T> JPAQuery<T> searchGradeCount(Expression<T> expression, SearchGradeQuery query) {
        var student = new QUser("student");
        var statement = new JPAQuery<Grade>(em)
                .select(expression)
                .from(grade)
                .join(grade.student, student);

        // Filter
        if (!Strings.isNullOrEmpty(query.getUserId())) {
            statement.where(student.id.eq(query.getUserId()));
        }

        return statement;
    }
}
