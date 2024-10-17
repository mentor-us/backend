ALTER TABLE votes
    ADD updated_date TIMESTAMP WITHOUT TIME ZONE;

UPDATE votes
SET updated_date = created_date;

ALTER TABLE votes
    ALTER COLUMN updated_date SET NOT NULL;

ALTER TABLE choices
    ADD updated_date TIMESTAMP WITHOUT TIME ZONE,
    ADD created_date TIMESTAMP WITHOUT TIME ZONE;

UPDATE choices
SET created_date = current_timestamp;

UPDATE choices
SET updated_date = created_date;

ALTER TABLE choices
    ALTER COLUMN updated_date SET NOT NULL,
    ALTER COLUMN created_date SET NOT NULL;