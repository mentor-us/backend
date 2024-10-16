package com.hcmus.mentor.backend.repository.custom;

import com.hcmus.mentor.backend.controller.usecase.auditrecord.search.SearchAuditRecordQuery;
import com.hcmus.mentor.backend.domain.AuditRecord;
import org.springframework.data.domain.Page;

public interface AuditRecordRepositoryCustom {

    Page<AuditRecord> search(SearchAuditRecordQuery query);
}
