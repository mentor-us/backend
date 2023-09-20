package com.hcmus.mentor.backend.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UpdateStudentInformationRequest {
    private Integer trainingPoint;
    private Boolean hasEnglishCert;
    private Double studyingPoint;
}
