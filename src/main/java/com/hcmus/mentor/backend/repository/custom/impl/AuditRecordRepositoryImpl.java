package com.hcmus.mentor.backend.repository.custom.impl;

import com.google.common.base.Strings;
import com.hcmus.mentor.backend.controller.usecase.auditrecord.search.SearchAuditRecordQuery;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.repository.custom.AuditRecordRepositoryCustom;
import com.hcmus.mentor.backend.util.OrderUtil;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.hcmus.mentor.backend.domain.QAuditRecord.auditRecord;
import static com.hcmus.mentor.backend.domain.QUser.user;

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
                .from(auditRecord)
                .join(auditRecord.user, user).fetchJoin();

        // Filter
        applyFilter(query, statement);

        // Sorting
        if (!Strings.isNullOrEmpty(query.getOrderBy())) {
            OrderUtil.addOrderBy(
                    statement,
                    OrderUtil.parseSeparated(query.getOrderBy()),
                    List.of(MutablePair.of("id", auditRecord.id),
                            MutablePair.of("action", auditRecord.action),
                            MutablePair.of("domain", auditRecord.domain),
                            MutablePair.of("created", auditRecord.createdDate)
                    )
            );
        } else {
            statement.orderBy(auditRecord.createdDate.desc());
        }

        return statement;
    }

    private <T> JPAQuery<T> searchCount(Expression<T> expression, SearchAuditRecordQuery query) {
        var statement = new JPAQuery<AuditRecord>(em)
                .select(expression)
                .from(auditRecord)
                .join(auditRecord.user, user);

        // Filter
        applyFilter(query, statement);

        return statement;
    }

    private static <T> void applyFilter(SearchAuditRecordQuery query, JPAQuery<T> statement) {
        if (!Strings.isNullOrEmpty(query.getUserName())) {
            statement.where(user.name.containsIgnoreCase(query.getUserName()));
        }
        if (!Strings.isNullOrEmpty(query.getUserEmail())) {
            statement.where(user.email.containsIgnoreCase(query.getUserEmail()));
        }
        Optional.ofNullable(query.getAction()).ifPresent(action -> statement.where(auditRecord.action.eq(action)));
        Optional.ofNullable(query.getDomain()).ifPresent(domain -> statement.where(auditRecord.domain.eq(domain)));
        Optional.ofNullable(query.getFrom()).ifPresent(from -> statement.where(auditRecord.createdDate.goe(Date.from(from.atZone(ZoneOffset.UTC).toInstant()))));
        Optional.ofNullable(query.getTo()).ifPresent(to -> statement.where(auditRecord.createdDate.loe(Date.from(to.atZone(ZoneOffset.UTC).toInstant()))));
    }
}
