-- Convert bytea data to JSON
ALTER TABLE reminder_properties
    ALTER COLUMN value TYPE JSONB
        USING CASE
                  WHEN value::text IS NULL THEN '{}'::JSONB
                  ELSE value::TEXT::JSONB
        END;