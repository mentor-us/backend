package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.domain.SchoolYear;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Tag(name = "years")
@RestController
@RequestMapping("api/years")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class SchoolYearController {

    private final Pipeline pipeline;

    @GetMapping("")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<List<SchoolYear>> getSchoolYear() {
        return ResponseEntity.ok(Collections.singletonList(new SchoolYear()));
    }

    @PostMapping("")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SchoolYear> createSchoolYear() {
        return ResponseEntity.ok(new SchoolYear());
    }

    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SchoolYear> updateSchoolYear(@PathVariable String id) {
        return ResponseEntity.ok(new SchoolYear());
    }

    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<Void> deleteSchoolYear(@PathVariable String id) {
        return ResponseEntity.ok().build();
    }
}