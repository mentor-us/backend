package com.hcmus.mentor.backend.controller.usecase.grade.common;

import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.Course;
import com.hcmus.mentor.backend.domain.SchoolYear;
import com.hcmus.mentor.backend.domain.Semester;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeDto {

    private String id;
    private Double score = 0.0;
    private boolean verified = false;
    private boolean isRetake = false;
    private ShortProfile student;
    private ShortProfile creator;
    private Semester semester;
    private SchoolYear year;
    private Course course;
    private Date createdDate;
    private Date updatedDate;
}