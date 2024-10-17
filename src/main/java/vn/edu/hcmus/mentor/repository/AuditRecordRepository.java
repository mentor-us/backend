package vn.edu.hcmus.mentor.repository;

import vn.edu.hcmus.mentor.domain.AuditRecord;
import vn.edu.hcmus.mentor.repository.custom.AuditRecordRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRecordRepository extends JpaRepository<AuditRecord, String>, AuditRecordRepositoryCustom {
}
