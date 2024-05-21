package com.hcmus.mentor.backend.controller.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RescheduleMeetingRequest {

    @NotNull
    private LocalDateTime timeStart;

    @NotNull
    private LocalDateTime timeEnd;

    private String place;
}
