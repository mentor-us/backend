CREATE TABLE channels
(
    id              VARCHAR(255) NOT NULL,
    name            VARCHAR(255),
    description     VARCHAR(255),
    image_url       VARCHAR(255),
    has_new_message BOOLEAN,
    created_date    TIMESTAMP WITHOUT TIME ZONE,
    updated_date    TIMESTAMP WITHOUT TIME ZONE,
    deleted_date    TIMESTAMP WITHOUT TIME ZONE,
    status          VARCHAR(255) NOT NULL,
    type            VARCHAR(255) NOT NULL,
    is_private      BOOLEAN      NOT NULL,
    last_message_id VARCHAR(255),
    creator_id      VARCHAR(255) NOT NULL,
    group_id        VARCHAR(255) NOT NULL,
    CONSTRAINT pk_channels PRIMARY KEY (id)
);

CREATE TABLE choices
(
    id         VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    vote_id    VARCHAR(255),
    creator_id VARCHAR(255),
    CONSTRAINT pk_choices PRIMARY KEY (id)
);

CREATE TABLE faqs
(
    id           VARCHAR(255) NOT NULL,
    question     VARCHAR(255) NOT NULL,
    answer       VARCHAR(255),
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    creator_id   VARCHAR(255),
    group_id     VARCHAR(255),
    CONSTRAINT pk_faqs PRIMARY KEY (id)
);

CREATE TABLE files
(
    id       VARCHAR(255) NOT NULL,
    filename VARCHAR(255),
    size     BIGINT       NOT NULL,
    url      VARCHAR(255),
    CONSTRAINT pk_files PRIMARY KEY (id)
);

CREATE TABLE group_category_permissions
(
    category_id VARCHAR(255) NOT NULL,
    permission  VARCHAR(255) NOT NULL
);

CREATE TABLE group_user
(
    id        VARCHAR(255) NOT NULL,
    is_mentor BOOLEAN,
    is_pinned BOOLEAN,
    is_marked BOOLEAN,
    group_id  VARCHAR(255),
    user_id   VARCHAR(255),
    CONSTRAINT pk_group_user PRIMARY KEY (id)
);

CREATE TABLE groups
(
    id                 VARCHAR(255) NOT NULL,
    name               VARCHAR(255) NOT NULL,
    description        VARCHAR(255),
    status             VARCHAR(255) NOT NULL,
    image_url          VARCHAR(255),
    has_new_message    BOOLEAN,
    time_start         TIMESTAMP WITHOUT TIME ZONE,
    time_end           TIMESTAMP WITHOUT TIME ZONE,
    duration           BIGINT,
    created_date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_message_id    VARCHAR(255),
    default_channel_id VARCHAR(255),
    group_category_id  VARCHAR(255),
    creator_id         VARCHAR(255),
    CONSTRAINT pk_groups PRIMARY KEY (id)
);

CREATE TABLE groups_categories
(
    id           VARCHAR(255) NOT NULL,
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(255),
    icon_url     VARCHAR(255),
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status       VARCHAR(255) NOT NULL,
    CONSTRAINT pk_groups_categories PRIMARY KEY (id)
);

CREATE TABLE meeting_histories
(
    id          VARCHAR(255) NOT NULL,
    time_start  TIMESTAMP WITHOUT TIME ZONE,
    time_end    TIMESTAMP WITHOUT TIME ZONE,
    place       VARCHAR(255),
    modify_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modifier_id VARCHAR(255),
    meeting_id  VARCHAR(255),
    CONSTRAINT pk_meeting_histories PRIMARY KEY (id)
);

CREATE TABLE meetings
(
    id           VARCHAR(255) NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  VARCHAR(255),
    time_start   TIMESTAMP WITHOUT TIME ZONE,
    time_end     TIMESTAMP WITHOUT TIME ZONE,
    repeated     VARCHAR(255),
    place        VARCHAR(255),
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_deleted   BOOLEAN      NOT NULL,
    deleted_date TIMESTAMP WITHOUT TIME ZONE,
    organizer_id VARCHAR(255),
    channel_id   VARCHAR(255),
    CONSTRAINT pk_meetings PRIMARY KEY (id)
);

CREATE TABLE message_images
(
    message_id VARCHAR(255) NOT NULL,
    images     VARCHAR(255)
);

CREATE TABLE messages
(
    id                VARCHAR(255) NOT NULL,
    content           VARCHAR(255),
    created_date      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_edited         BOOLEAN      NOT NULL,
    edited_at         TIMESTAMP WITHOUT TIME ZONE,
    type              VARCHAR(255) NOT NULL,
    status            VARCHAR(255) NOT NULL,
    reply             VARCHAR(255),
    is_forward        BOOLEAN      NOT NULL,
    sender_id         VARCHAR(255),
    channel_id        VARCHAR(255),
    vote_id           VARCHAR(255),
    file_id           VARCHAR(255),
    meeting_id        VARCHAR(255),
    task_id           VARCHAR(255),
    channel_pinned_id VARCHAR(255),
    message_pinned_id VARCHAR(255),
    CONSTRAINT pk_messages PRIMARY KEY (id)
);

CREATE TABLE notification_subscriber
(
    id           VARCHAR(255) NOT NULL,
    token        VARCHAR(255) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id      VARCHAR(255),
    CONSTRAINT pk_notification_subscriber PRIMARY KEY (id)
);

CREATE TABLE notification_user
(
    id              VARCHAR(255) NOT NULL,
    is_readed       BOOLEAN      NOT NULL,
    is_deleted      BOOLEAN      NOT NULL,
    is_agreed       BOOLEAN,
    notification_id VARCHAR(255),
    user_id         VARCHAR(255),
    CONSTRAINT pk_notification_user PRIMARY KEY (id)
);

CREATE TABLE notifications
(
    id           VARCHAR(255) NOT NULL,
    title        VARCHAR(255),
    content      VARCHAR(255),
    type         VARCHAR(255) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    ref_id       VARCHAR(255),
    sender_id    VARCHAR(255),
    CONSTRAINT pk_notifications PRIMARY KEY (id)
);

CREATE TABLE reactions
(
    id         VARCHAR(255) NOT NULL,
    name       VARCHAR(255),
    image_url  VARCHAR(255),
    total      INTEGER,
    emoji_type VARCHAR(255) NOT NULL,
    user_id    VARCHAR(255),
    message_id VARCHAR(255),
    CONSTRAINT pk_reactions PRIMARY KEY (id)
);

CREATE TABLE ref_choice_user
(
    choice_id VARCHAR(255) NOT NULL,
    voter_id  VARCHAR(255) NOT NULL
);

CREATE TABLE rel_faq_user_voter
(
    faq_id  VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL
);

CREATE TABLE rel_user_channel
(
    channel_id VARCHAR(255) NOT NULL,
    user_id    VARCHAR(255) NOT NULL
);

CREATE TABLE rel_user_meeting_attendees
(
    meeting_id VARCHAR(255) NOT NULL,
    user_id    VARCHAR(255) NOT NULL
);

CREATE TABLE rel_user_reminder
(
    reminder_id VARCHAR(255) NOT NULL,
    user_id     VARCHAR(255) NOT NULL
);

CREATE TABLE reminder_properties
(
    reminder_id VARCHAR(255) NOT NULL,
    value       BYTEA,
    key         VARCHAR(255) NOT NULL,
    CONSTRAINT pk_reminder_properties PRIMARY KEY (reminder_id, key)
);

CREATE TABLE reminders
(
    id            VARCHAR(255) NOT NULL,
    type          VARCHAR(255) NOT NULL,
    subject       VARCHAR(255),
    content       VARCHAR(255),
    reminder_date TIMESTAMP WITHOUT TIME ZONE,
    remindable_id VARCHAR(255),
    channel_id    VARCHAR(255),
    CONSTRAINT pk_reminders PRIMARY KEY (id)
);

CREATE TABLE "system-config"
(
    id          VARCHAR(255) NOT NULL,
    name        VARCHAR(255),
    description VARCHAR(255),
    type        VARCHAR(255),
    key         VARCHAR(255),
    value       TEXT,
    CONSTRAINT "pk_system-config" PRIMARY KEY (id)
);

CREATE TABLE task_assignee
(
    id      VARCHAR(255) NOT NULL,
    status  VARCHAR(255),
    task_id VARCHAR(255),
    user_id VARCHAR(255),
    CONSTRAINT pk_task_assignee PRIMARY KEY (id)
);

CREATE TABLE tasks
(
    id             VARCHAR(255) NOT NULL,
    title          VARCHAR(255) NOT NULL,
    description    VARCHAR(255),
    deadline       TIMESTAMP WITHOUT TIME ZONE,
    created_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_deleted     BOOLEAN      NOT NULL,
    deleted_date   TIMESTAMP WITHOUT TIME ZONE,
    assigner_id    VARCHAR(255) NOT NULL,
    parent_task_id VARCHAR(255),
    channel_id     VARCHAR(255) NOT NULL,
    CONSTRAINT pk_tasks PRIMARY KEY (id)
);

CREATE TABLE user_additional_emails
(
    user_id           VARCHAR(255) NOT NULL,
    additional_emails VARCHAR(255)
);

CREATE TABLE user_roles
(
    user_id VARCHAR(255) NOT NULL,
    roles   SMALLINT
);

CREATE TABLE users
(
    id               VARCHAR(255) NOT NULL,
    name             VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    image_url        VARCHAR(255),
    wallpaper        VARCHAR(255),
    email_verified   BOOLEAN,
    password         VARCHAR(255),
    provider         VARCHAR(255) NOT NULL,
    provider_id      VARCHAR(255),
    status           BOOLEAN,
    phone            VARCHAR(255),
    birth_date       TIMESTAMP WITHOUT TIME ZONE,
    created_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    training_point   INTEGER,
    has_english_cert BOOLEAN,
    studying_point   DOUBLE PRECISION,
    initial_name     VARCHAR(255),
    gender           VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE votes
(
    id                 VARCHAR(255) NOT NULL,
    question           VARCHAR(255),
    time_end           TIMESTAMP WITHOUT TIME ZONE,
    closed_date        TIMESTAMP WITHOUT TIME ZONE,
    created_date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_deleted         BOOLEAN      NOT NULL,
    deleted_date       TIMESTAMP WITHOUT TIME ZONE,
    is_multiple_choice BOOLEAN      NOT NULL,
    status             VARCHAR(255) NOT NULL,
    creator_id         VARCHAR(255),
    channel_id         VARCHAR(255),
    CONSTRAINT pk_votes PRIMARY KEY (id)
);

ALTER TABLE channels
    ADD CONSTRAINT uc_channels_last_message UNIQUE (last_message_id);

ALTER TABLE groups
    ADD CONSTRAINT uc_groups_default_channel UNIQUE (default_channel_id);

ALTER TABLE groups
    ADD CONSTRAINT uc_groups_last_message UNIQUE (last_message_id);

ALTER TABLE messages
    ADD CONSTRAINT uc_messages_file UNIQUE (file_id);

ALTER TABLE messages
    ADD CONSTRAINT uc_messages_meeting UNIQUE (meeting_id);

ALTER TABLE messages
    ADD CONSTRAINT uc_messages_task UNIQUE (task_id);

ALTER TABLE messages
    ADD CONSTRAINT uc_messages_vote UNIQUE (vote_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

CREATE UNIQUE INDEX idx_reaction ON reactions (message_id, emoji_type, user_id);

ALTER TABLE channels
    ADD CONSTRAINT FK_CHANNELS_ON_CREATOR FOREIGN KEY (creator_id) REFERENCES users (id);

ALTER TABLE channels
    ADD CONSTRAINT FK_CHANNELS_ON_GROUP FOREIGN KEY (group_id) REFERENCES groups (id);

ALTER TABLE channels
    ADD CONSTRAINT FK_CHANNELS_ON_LAST_MESSAGE FOREIGN KEY (last_message_id) REFERENCES messages (id);

ALTER TABLE choices
    ADD CONSTRAINT FK_CHOICES_ON_CREATOR FOREIGN KEY (creator_id) REFERENCES users (id);

ALTER TABLE choices
    ADD CONSTRAINT FK_CHOICES_ON_VOTE FOREIGN KEY (vote_id) REFERENCES votes (id);

ALTER TABLE faqs
    ADD CONSTRAINT FK_FAQS_ON_CREATOR FOREIGN KEY (creator_id) REFERENCES users (id);

ALTER TABLE faqs
    ADD CONSTRAINT FK_FAQS_ON_GROUP FOREIGN KEY (group_id) REFERENCES groups (id);

ALTER TABLE groups
    ADD CONSTRAINT FK_GROUPS_ON_CREATOR FOREIGN KEY (creator_id) REFERENCES users (id);

ALTER TABLE groups
    ADD CONSTRAINT FK_GROUPS_ON_DEFAULT_CHANNEL FOREIGN KEY (default_channel_id) REFERENCES channels (id);

ALTER TABLE groups
    ADD CONSTRAINT FK_GROUPS_ON_GROUP_CATEGORY FOREIGN KEY (group_category_id) REFERENCES groups_categories (id);

ALTER TABLE groups
    ADD CONSTRAINT FK_GROUPS_ON_LAST_MESSAGE FOREIGN KEY (last_message_id) REFERENCES messages (id);

ALTER TABLE group_user
    ADD CONSTRAINT FK_GROUP_USER_ON_GROUP FOREIGN KEY (group_id) REFERENCES groups (id);

ALTER TABLE group_user
    ADD CONSTRAINT FK_GROUP_USER_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE meetings
    ADD CONSTRAINT FK_MEETINGS_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channels (id);

ALTER TABLE meetings
    ADD CONSTRAINT FK_MEETINGS_ON_ORGANIZER FOREIGN KEY (organizer_id) REFERENCES users (id);

ALTER TABLE meeting_histories
    ADD CONSTRAINT FK_MEETING_HISTORIES_ON_MEETING FOREIGN KEY (meeting_id) REFERENCES meetings (id);

ALTER TABLE meeting_histories
    ADD CONSTRAINT FK_MEETING_HISTORIES_ON_MODIFIER FOREIGN KEY (modifier_id) REFERENCES users (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channels (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_CHANNEL_PINNED FOREIGN KEY (channel_pinned_id) REFERENCES channels (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_FILE FOREIGN KEY (file_id) REFERENCES files (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_MEETING FOREIGN KEY (meeting_id) REFERENCES meetings (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_MESSAGE_PINNED FOREIGN KEY (message_pinned_id) REFERENCES groups (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_SENDER FOREIGN KEY (sender_id) REFERENCES users (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_TASK FOREIGN KEY (task_id) REFERENCES tasks (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_VOTE FOREIGN KEY (vote_id) REFERENCES votes (id);

ALTER TABLE notifications
    ADD CONSTRAINT FK_NOTIFICATIONS_ON_SENDER FOREIGN KEY (sender_id) REFERENCES users (id);

ALTER TABLE notification_subscriber
    ADD CONSTRAINT FK_NOTIFICATION_SUBSCRIBER_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE notification_user
    ADD CONSTRAINT FK_NOTIFICATION_USER_ON_NOTIFICATION FOREIGN KEY (notification_id) REFERENCES notifications (id);

ALTER TABLE notification_user
    ADD CONSTRAINT FK_NOTIFICATION_USER_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE reactions
    ADD CONSTRAINT FK_REACTIONS_ON_MESSAGE FOREIGN KEY (message_id) REFERENCES messages (id);

ALTER TABLE reactions
    ADD CONSTRAINT FK_REACTIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE reminders
    ADD CONSTRAINT FK_REMINDERS_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channels (id);

ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_ASSIGNER FOREIGN KEY (assigner_id) REFERENCES users (id);

ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channels (id);

ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_PARENT_TASK FOREIGN KEY (parent_task_id) REFERENCES tasks (id);

ALTER TABLE task_assignee
    ADD CONSTRAINT FK_TASK_ASSIGNEE_ON_TASK FOREIGN KEY (task_id) REFERENCES tasks (id);

ALTER TABLE task_assignee
    ADD CONSTRAINT FK_TASK_ASSIGNEE_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE votes
    ADD CONSTRAINT FK_VOTES_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channels (id);

ALTER TABLE votes
    ADD CONSTRAINT FK_VOTES_ON_CREATOR FOREIGN KEY (creator_id) REFERENCES users (id);

ALTER TABLE group_category_permissions
    ADD CONSTRAINT fk_group_category_permissions_on_group_category FOREIGN KEY (category_id) REFERENCES groups_categories (id);

ALTER TABLE message_images
    ADD CONSTRAINT fk_message_images_on_message FOREIGN KEY (message_id) REFERENCES messages (id);

ALTER TABLE ref_choice_user
    ADD CONSTRAINT fk_refchouse_on_choice FOREIGN KEY (choice_id) REFERENCES choices (id);

ALTER TABLE ref_choice_user
    ADD CONSTRAINT fk_refchouse_on_user FOREIGN KEY (voter_id) REFERENCES users (id);

ALTER TABLE rel_faq_user_voter
    ADD CONSTRAINT fk_relfaqusevot_on_faq FOREIGN KEY (faq_id) REFERENCES faqs (id);

ALTER TABLE rel_faq_user_voter
    ADD CONSTRAINT fk_relfaqusevot_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE rel_user_channel
    ADD CONSTRAINT fk_relusecha_on_channel FOREIGN KEY (channel_id) REFERENCES channels (id);

ALTER TABLE rel_user_channel
    ADD CONSTRAINT fk_relusecha_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE rel_user_meeting_attendees
    ADD CONSTRAINT fk_relusemeeatt_on_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id);

ALTER TABLE rel_user_meeting_attendees
    ADD CONSTRAINT fk_relusemeeatt_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE rel_user_reminder
    ADD CONSTRAINT fk_reluserem_on_reminder FOREIGN KEY (reminder_id) REFERENCES reminders (id);

ALTER TABLE rel_user_reminder
    ADD CONSTRAINT fk_reluserem_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE reminder_properties
    ADD CONSTRAINT fk_reminder_properties_on_reminder FOREIGN KEY (reminder_id) REFERENCES reminders (id);

ALTER TABLE user_additional_emails
    ADD CONSTRAINT fk_user_additionalemails_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_on_user FOREIGN KEY (user_id) REFERENCES users (id);
