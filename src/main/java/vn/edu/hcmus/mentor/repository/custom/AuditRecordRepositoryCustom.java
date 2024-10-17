package vn.edu.hcmus.mentor.repository.custom;

import vn.edu.hcmus.mentor.controller.usecase.auditrecord.search.SearchAuditRecordQuery;
import vn.edu.hcmus.mentor.domain.AuditRecord;
import org.springframework.data.domain.Page;

public interface AuditRecordRepositoryCustom {

    Page<AuditRecord> search(SearchAuditRecordQuery query);
}
