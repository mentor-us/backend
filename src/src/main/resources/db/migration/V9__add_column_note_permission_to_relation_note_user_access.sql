ALTER TABLE note_user_access
    ADD COLUMN note_permission VARCHAR(255) NOT NULL DEFAULT 'VIEW';