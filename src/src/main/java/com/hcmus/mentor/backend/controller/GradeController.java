package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.usecase.grade.creategrade.CreateGradeCommand;
import com.hcmus.mentor.backend.controller.usecase.grade.updategrade.UpdateGradeCommand;
import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "grades")
@RestController
@RequestMapping("api/grades")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class GradeController {

    @GetMapping("")
    public ResponseEntity<List<Grade>> getGrade(@Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedInUser) {
        return ResponseEntity.ok(List.of(new Grade()));
    }

    @PostMapping("")
    public ResponseEntity<Grade> createGrade(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedInUser,
            @RequestBody CreateGradeCommand command) {
        return ResponseEntity.ok(new Grade());
    }

    @PatchMapping("{id}")
    public ResponseEntity<Grade> updateGrade(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedInUser,
            @PathVariable String id, @RequestBody UpdateGradeCommand command) {
        return ResponseEntity.ok(new Grade());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteGrade(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedInUser,
            @PathVariable String id) {
        return ResponseEntity.ok().build();
    }
}