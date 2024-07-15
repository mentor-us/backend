ALTER TABLE entity_audit_records
    RENAME TO audit_record;

ALTER TABLE audit_record
    RENAME CONSTRAINT pk_entity_audit_records TO pk_audit_records;

ALTER TABLE audit_record
    RENAME CONSTRAINT FK_ENTITY_AUDIT_RECORDS_ON_USERS TO FK_AUDIT_RECORDS_ON_USERS;