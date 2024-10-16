package com.hcmus.mentor.backend.controller.payload.response.analytic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImportGeneralInformationResponse {
    private String email;
    private String trainingPoint;
    private String hasEnglishCert;
    private String studyingPoint;
}
