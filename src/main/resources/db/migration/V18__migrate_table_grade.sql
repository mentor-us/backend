ALTER TABLE grades
    DROP CONSTRAINT IF EXISTS FK_GRADES_ON_SEMESTER;

ALTER TABLE grades
    DROP COLUMN IF EXISTS semester_id;

ALTER TABLE grades
    ADD COLUMN IF NOT EXISTS semester INT;

DROP TABLE IF EXISTS semesters;

------------------------------------------------------------------------------------------------------------------------
ALTER TABLE grades
    DROP CONSTRAINT IF EXISTS FK_GRADES_ON_COURSE;

ALTER TABLE grades
    DROP COLUMN IF EXISTS course_id;

ALTER TABLE grades
    DROP COLUMN IF EXISTS verified;

ALTER TABLE grades
    ADD COLUMN IF NOT EXISTS course_name VARCHAR(255);

ALTER TABLE grades
    ADD COLUMN IF NOT EXISTS course_code VARCHAR(255);

DROP TABLE IF EXISTS courses;

------------------------------------------------------------------------------------------------------------------------

ALTER TABLE grades
    DROP CONSTRAINT IF EXISTS FK_GRADES_ON_YEAR;

DO
$$
    BEGIN
        IF EXISTS(SELECT column_name
                  FROM information_schema.columns
                  WHERE table_name = 'grades'
                    AND column_name = 'year_id')
        THEN
            ALTER TABLE "public"."grades"
                RENAME COLUMN "year_id" TO "year";
        END IF;
    END
$$;

DROP TABLE IF EXISTS school_years;

------------------------------------------------------------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_grades_info ON grades (year, semester, course_code, course_name);

CREATE INDEX IF NOT EXISTS idx_grades_student ON grades (student_id);