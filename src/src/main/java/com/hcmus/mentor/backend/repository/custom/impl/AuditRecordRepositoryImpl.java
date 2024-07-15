package com.hcmus.mentor.backend.repository.custom.impl;

import com.hcmus.mentor.backend.controller.usecase.auditrecord.search.SearchAuditRecordQuery;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.repository.custom.AuditRecordRepositoryCustom;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Optional;

import static com.hcmus.mentor.backend.domain.QAuditRecord.auditRecord;

public class AuditRecordRepositoryImpl extends QuerydslRepositorySupport implements AuditRecordRepositoryCustom {

    private final EntityManager em;

    public AuditRecordRepositoryImpl(EntityManager em) {
        super(AuditRecord.class);
        this.em = em;
    }

    @Override
    public Page<AuditRecord> search(SearchAuditRecordQuery query) {
        var statement = searchResult(auditRecord, query);

        // Paging
        var pageable = PageRequest.of(query.getPage(), query.getPageSize());
        Optional.ofNullable(getQuerydsl()).ifPresent(querydsl -> querydsl.applyPagination(pageable, statement));

        var rows = statement.fetch();
        var total = searchCount(auditRecord.count(), query).fetchOne();

        return PageableExecutionUtils.getPage(rows, pageable, () -> Optional.ofNullable(total).orElse(0L));
    }

    private <T> JPAQuery<T> searchResult(Expression<T> expression, SearchAuditRecordQuery query) {
        var statement = new JPAQuery<AuditRecord>(em)
                .select(expression)
                .from(auditRecord);

        // Filter

        // Sorting

        return statement;
    }

    private <T> JPAQuery<T> searchCount(Expression<T> expression, SearchAuditRecordQuery query) {
        var statement = new JPAQuery<AuditRecord>(em)
                .select(expression)
                .from(auditRecord);

        // Filter

        return statement;
    }
}
