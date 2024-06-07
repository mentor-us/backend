package com.hcmus.mentor.backend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "semesters")
@RestController
@RequestMapping("api/years")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class SemesterController {
}