package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.usecase.auditrecord.search.SearchAuditRecordQuery;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.repository.AuditRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditRecordService {

    private final AuditRecordRepository auditRecordRepository;

    public Page<AuditRecord> search(SearchAuditRecordQuery query) {
        return auditRecordRepository.search(query);
    }

    public AuditRecord save(AuditRecord auditRecord) {
        return auditRecordRepository.save(auditRecord);
    }
}
