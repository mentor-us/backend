DELETE
FROM grade_histories
WHERE grade_version_id = 'cf12ee92-30fb-4154-b67b-bc14f355a4b0';
INSERT INTO grade_histories (id, score, value, semester, year, course_name, course_code, grade_version_id, created_date,
                             updated_date)
SELECT g.id,
       g.score,
       g.value,
       g.semester,
       g.year,
       g.course_name,
       g.course_code,
       null,
       g.created_date,
       g.updated_date
FROM grades g;