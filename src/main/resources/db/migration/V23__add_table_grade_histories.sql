CREATE TABLE IF NOT EXISTS grade_histories
(
    id               varchar(255)                NOT NULL PRIMARY KEY,
    created_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    score            DOUBLE PRECISION,
    value            VARCHAR(3),
    semester         INT,
    year             VARCHAR(255),
    course_name      VARCHAR(255),
    course_code      VARCHAR(255),
    grade_version_id VARCHAR(255),
    CONSTRAINT FK_GRADE_HISTORIES_ON_GRADE_VERSIONS FOREIGN KEY (grade_version_id) REFERENCES grade_versions (id)

);