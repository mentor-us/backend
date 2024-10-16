CREATE TABLE IF NOT EXISTS grade_user_access
(
    id             VARCHAR(255)                NOT NULL,
    user_id        VARCHAR(255)                NOT NULL,
    user_access_id VARCHAR(255)                NOT NULL,
    created_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    CONSTRAINT pk_grade_user_access PRIMARY KEY (id),
    CONSTRAINT fk_grade_user_access__user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_grade_user_access__user_access FOREIGN KEY (user_access_id) REFERENCES users (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_user_access_unit ON grade_user_access (user_id, user_access_id);