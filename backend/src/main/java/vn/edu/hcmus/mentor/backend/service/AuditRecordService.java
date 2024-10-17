package vn.edu.hcmus.mentor.backend.service;

import vn.edu.hcmus.mentor.backend.controller.usecase.auditrecord.search.SearchAuditRecordQuery;
import vn.edu.hcmus.mentor.backend.domain.AuditRecord;
import vn.edu.hcmus.mentor.backend.repository.AuditRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditRecordService {

    private final AuditRecordRepository auditRecordRepository;

    public Page<AuditRecord> search(SearchAuditRecordQuery query) {
        return auditRecordRepository.search(query);
    }

    public void saveAll(List<AuditRecord> auditRecords) {
        auditRecordRepository.saveAll(auditRecords);
    }

    public AuditRecord save(AuditRecord auditRecord) {
        return auditRecordRepository.save(auditRecord);
    }

    public void delete(AuditRecord record) {
        auditRecordRepository.delete(record);
    }

    public void delete(List<AuditRecord> records) {
        auditRecordRepository.deleteAll(records);
    }
}