ALTER TABLE users
    ADD updated_date TIMESTAMP WITHOUT TIME ZONE;

UPDATE users
    SET updated_date = created_date;

ALTER TABLE users
    ALTER COLUMN updated_date SET NOT NULL;

CREATE UNIQUE INDEX IX_pk_groups ON groups (id, id);