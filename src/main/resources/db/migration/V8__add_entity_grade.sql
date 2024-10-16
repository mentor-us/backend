CREATE TABLE school_years
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    name         VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_school_years PRIMARY KEY (id)
);

CREATE TABLE semesters
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    name         VARCHAR(255)                NOT NULL,
    year_id      VARCHAR(255),
    CONSTRAINT pk_semesters PRIMARY KEY (id),
    CONSTRAINT FK_SEMESTERS_ON_YEAR FOREIGN KEY (year_id) REFERENCES school_years (id)
);

CREATE TABLE grades
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    name         VARCHAR(255)                NOT NULL,
    score        DOUBLE PRECISION            NOT NULL DEFAULT 0.0,
    verified     BOOLEAN                     NOT NULL DEFAULT FALSE,
    student_id   VARCHAR(255),
    semester_id  VARCHAR(255),
    CONSTRAINT pk_grades PRIMARY KEY (id),
    CONSTRAINT FK_GRADES_ON_SEMESTER FOREIGN KEY (semester_id) REFERENCES semesters (id),
    CONSTRAINT FK_GRADES_ON_STUDENT FOREIGN KEY (student_id) REFERENCES users (id)
);