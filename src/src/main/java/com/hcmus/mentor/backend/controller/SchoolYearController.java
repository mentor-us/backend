package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.usecase.schoolyear.common.SchoolYearDto;
import com.hcmus.mentor.backend.controller.usecase.schoolyear.create.CreateSchoolYearCommand;
import com.hcmus.mentor.backend.controller.usecase.schoolyear.delete.DeleteSchoolYearCommand;
import com.hcmus.mentor.backend.controller.usecase.schoolyear.getbyid.GetSchoolYearByIdQuery;
import com.hcmus.mentor.backend.controller.usecase.schoolyear.search.SearchSchoolYearQuery;
import com.hcmus.mentor.backend.controller.usecase.schoolyear.search.SearchSchoolYearResult;
import com.hcmus.mentor.backend.controller.usecase.schoolyear.update.UpdateSchoolYearCommand;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "years")
@RestController
@RequestMapping("api/years")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class SchoolYearController {

    private final Pipeline pipeline;

    @GetMapping("")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SearchSchoolYearResult> search(SearchSchoolYearQuery query) {
        var result = pipeline.send(query);

        return ResponseEntity.ok(result);
    }

    @GetMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SchoolYearDto> getById(@PathVariable String id) {
        var query = GetSchoolYearByIdQuery.builder().id(id).build();

        return ResponseEntity.ok(pipeline.send(query));
    }

    @PostMapping("")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SchoolYearDto> create(
            @RequestBody CreateSchoolYearCommand command) {
        return ResponseEntity.ok(pipeline.send(command));
    }

    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SchoolYearDto> update(
            @PathVariable String id,
            @RequestBody UpdateSchoolYearCommand command) {
        command.setId(id);

        return ResponseEntity.ok(pipeline.send(command));
    }

    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SchoolYearDto> delete(@PathVariable String id) {
        var command = DeleteSchoolYearCommand.builder().id(id).build();

        return ResponseEntity.ok(pipeline.send(command));
    }
}