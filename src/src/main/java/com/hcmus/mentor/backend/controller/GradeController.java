package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import com.hcmus.mentor.backend.controller.usecase.grade.creategrade.CreateGradeCommand;
import com.hcmus.mentor.backend.controller.usecase.grade.deletegrade.DeleteGradeCommand;
import com.hcmus.mentor.backend.controller.usecase.grade.getgrade.SearchGradeQuery;
import com.hcmus.mentor.backend.controller.usecase.grade.getgrade.SearchGradeResult;
import com.hcmus.mentor.backend.controller.usecase.grade.updategrade.UpdateGradeCommand;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "grades")
@RestController
@RequestMapping("api/grades")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class GradeController {

    private final Pipeline pipeline;

    @GetMapping("")
    public ResponseEntity<SearchGradeResult> searchGrade(SearchGradeQuery query) {
        var result = pipeline.send(query);

        return ResponseEntity.ok(result);
    }

    @PostMapping("")
    public ResponseEntity<GradeDto> create(
            @RequestBody CreateGradeCommand command) {
        return ResponseEntity.ok(pipeline.send(command));
    }

    @PatchMapping("{id}")
    public ResponseEntity<GradeDto> updateGrade(
            @PathVariable String id, @RequestBody UpdateGradeCommand command) {
        command.setId(id);

        return ResponseEntity.ok(pipeline.send(command));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<GradeDto> deleteGrade(
            @PathVariable String id) {
        var command = DeleteGradeCommand.builder().id(id).build();

        return ResponseEntity.ok(pipeline.send(command));
    }
}