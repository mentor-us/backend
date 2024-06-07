CREATE TABLE notes
(
    id           VARCHAR(255)                NOT NULL,
    title        VARCHAR(255)                NOT NULL DEFAULT '',
    content      VARCHAR(255)                NOT NULL DEFAULT '',
    is_public    BOOLEAN                     NOT NULL DEFAULT FALSE,
    creator_id   VARCHAR(255)                NOT NULL,
    owner_id     VARCHAR(255)                NOT NULL,
    updated_by   VARCHAR(255),
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    CONSTRAINT pk_user_notes PRIMARY KEY (id),
    CONSTRAINT fk_note_creator FOREIGN KEY (creator_id) REFERENCES users (id),
    CONSTRAINT fk_note_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE ref_user_note
(
    user_id VARCHAR(255) NOT NULL,
    note_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user_note PRIMARY KEY (user_id, note_id),
    CONSTRAINT fk_user_note_note FOREIGN KEY (note_id) REFERENCES notes (id),
    CONSTRAINT fk_user_note_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE note_user_access
(
    id           VARCHAR(255)                NOT NULL,
    note_id      VARCHAR(255),
    user_id      VARCHAR(255),
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    CONSTRAINT pk_note_user_access PRIMARY KEY (id),
    CONSTRAINT fk_note_user_access_note FOREIGN KEY (note_id) REFERENCES notes (id),
    CONSTRAINT fk_note_user_access_user FOREIGN KEY (user_id) REFERENCES notes (id)
);

CREATE TABLE note_histories
(
    id           VARCHAR(255)                NOT NULL,
    title        VARCHAR(255)                NOT NULL DEFAULT '',
    content      VARCHAR(255)                NOT NULL DEFAULT '',
    note_id      VARCHAR(255)                NOT NULL,
    updated_by   VARCHAR(255),
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    CONSTRAINT pk_note_histories PRIMARY KEY (id),
    CONSTRAINT fk_note_histories_note FOREIGN KEY (note_id) REFERENCES notes (id),
    CONSTRAINT fk_note_histories_user FOREIGN KEY (updated_by) REFERENCES notes (id)
);

CREATE UNIQUE INDEX idx_note_user_access_unit ON note_user_access (user_id, note_id);