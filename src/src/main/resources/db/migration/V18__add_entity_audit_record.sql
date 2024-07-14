CREATE TABLE entity_audit_records
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    entity_id    VARCHAR(255)                NOT NULL,
    detail       text                        NOT NULL,
    domain       VARCHAR(255)                NOT NULL,
    action       VARCHAR(255)                NOT NULL,
    user_id      VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_entity_audit_records PRIMARY KEY (id),
    CONSTRAINT FK_ENTITY_AUDIT_RECORDS_ON_USERS FOREIGN KEY (user_id) REFERENCES users (id)
);