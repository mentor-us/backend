package vn.edu.hcmus.mentor.backend.repository.custom;

import vn.edu.hcmus.mentor.backend.controller.usecase.auditrecord.search.SearchAuditRecordQuery;
import vn.edu.hcmus.mentor.backend.domain.AuditRecord;
import org.springframework.data.domain.Page;

public interface AuditRecordRepositoryCustom {

    Page<AuditRecord> search(SearchAuditRecordQuery query);
}
