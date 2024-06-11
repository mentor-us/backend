ALTER TABLE note_histories
    DROP CONSTRAINT fk_note_histories_user;
ALTER TABLE note_histories
    ADD CONSTRAINT fk_note_histories_user FOREIGN KEY (updated_by) REFERENCES users (id);

ALTER TABLE note_user_access
    DROP CONSTRAINT fk_note_user_access_user;
ALTER TABLE note_user_access
    ADD CONSTRAINT fk_note_user_access_user FOREIGN KEY (user_id) REFERENCES users (id);