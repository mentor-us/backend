CREATE OR REPLACE VIEW list_mentors AS
SELECT gu1.user_id AS mentor_id,
       gu2.user_id AS mentee_id
FROM group_user gu1
         JOIN group_user gu2 ON gu1.group_id = gu2.group_id
WHERE gu1.is_mentor = TRUE
  AND gu2.is_mentor = FALSE
;