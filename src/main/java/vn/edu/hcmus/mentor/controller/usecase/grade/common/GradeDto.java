package vn.edu.hcmus.mentor.controller.usecase.grade.common;

import vn.edu.hcmus.mentor.controller.payload.response.users.ShortProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeDto {

    private String id;
    private Double score;
    private String value;
    private Boolean isRetake;
    private ShortProfile student;
    private ShortProfile creator;
    private Integer semester;
    private String year;
    private String courseCode;
    private String courseName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}