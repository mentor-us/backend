package com.hcmus.mentor.backend.controller.usecase.grade.common;

import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
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
    private Boolean isRetake;
    private ShortProfile student;
    private ShortProfile creator;
    private Integer semester;
    private String year;
    private String courseCode;
    private String courseName;
    private Date createdDate;
    private Date updatedDate;
}