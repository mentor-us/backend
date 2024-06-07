package com.hcmus.mentor.backend.controller.payload.request.meetings;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RescheduleMeetingRequest {

    @NotNull
    private Date timeStart;

    @NotNull
    private Date timeEnd;

    private String place;
}