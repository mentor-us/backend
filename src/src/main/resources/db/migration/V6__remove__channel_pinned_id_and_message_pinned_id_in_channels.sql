ALTER TABLE channels
    DROP CONSTRAINT IF EXISTS fk_messages_on_channel_pinned,
    DROP CONSTRAINT IF EXISTS fk_messages_on_message_pinned;

ALTER TABLE channels
    DROP COLUMN IF EXISTS channel_pinned_id,
    DROP COLUMN IF EXISTS message_pinned_id;

CREATE TABLE rel_channel_message_pin
(
    channel_id VARCHAR(255) NOT NULL,
    message_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_channel_user PRIMARY KEY (channel_id, message_id),
    CONSTRAINT fk_channel_on_message_pin FOREIGN KEY (channel_id) REFERENCES channels (id),
    CONSTRAINT fk_message_on_channel_pin FOREIGN KEY (message_id) REFERENCES messages (id)
);

ALTER TABLE rel_user_channel
    ADD CONSTRAINT pk_user_channel_member PRIMARY KEY (channel_id, user_id);

ALTER TABLE ref_choice_user
    ADD CONSTRAINT pk_choice_user PRIMARY KEY (choice_id, voter_id);

ALTER TABLE rel_faq_user_voter
    ADD CONSTRAINT pk_faq_user_voter PRIMARY KEY (faq_id, user_id);

ALTER TABLE rel_user_meeting_attendees
    ADD CONSTRAINT pk_user_meeting_attendees PRIMARY KEY (meeting_id, user_id);

ALTER TABLE rel_user_reminder
    ADD CONSTRAINT pk_user_reminder PRIMARY KEY (reminder_id, user_id);