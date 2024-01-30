package com.hcmus.mentor.backend.controller.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UpdateStudentInformationRequest {
    private Integer trainingPoint;
    private Boolean hasEnglishCert;
    private Double studyingPoint;
}
