package vn.edu.hcmus.mentor.backend.repository;

import vn.edu.hcmus.mentor.backend.domain.AuditRecord;
import vn.edu.hcmus.mentor.backend.repository.custom.AuditRecordRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRecordRepository extends JpaRepository<AuditRecord, String>, AuditRecordRepositoryCustom {
}
