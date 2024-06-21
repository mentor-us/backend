package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.usecase.semester.common.SemesterDto;
import com.hcmus.mentor.backend.controller.usecase.semester.create.CreateSemesterCommand;
import com.hcmus.mentor.backend.controller.usecase.semester.delete.DeleteSemesterCommand;
import com.hcmus.mentor.backend.controller.usecase.semester.getbyid.GetSemesterByIdQuery;
import com.hcmus.mentor.backend.controller.usecase.semester.search.SearchSemesterQuery;
import com.hcmus.mentor.backend.controller.usecase.semester.search.SearchSemesterResult;
import com.hcmus.mentor.backend.controller.usecase.semester.update.UpdateSemesterCommand;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Semester controller.
 */
@Tag(name = "semesters")
@RestController
@RequestMapping("api/semesters")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class SemesterController {

    private final Pipeline pipeline;

    /**
     * Search for semesters based on criteria.
     *
     * @param query Search criteria for semesters
     * @return ResponseEntity containing the search results
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SearchSemesterResult> search(SearchSemesterQuery query) {
        var result = pipeline.send(query);

        return ResponseEntity.ok(result);
    }

    /**
     * Retrieve a semester by its ID.
     *
     * @param id ID of the semester to retrieve
     * @return ResponseEntity containing the semester details
     */
    @GetMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SemesterDto> getById(
            @PathVariable String id) {
        var query = GetSemesterByIdQuery.builder().id(id).build();

        return ResponseEntity.ok(pipeline.send(query));
    }

    /**
     * Create a new semester.
     *
     * @param command Command containing semester details for creation
     * @return ResponseEntity containing the created semester details
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SemesterDto> create(
            @RequestBody CreateSemesterCommand command) {
        return ResponseEntity.ok(pipeline.send(command));
    }

    /**
     * Update an existing semester.
     *
     * @param id      ID of the semester to update
     * @param command Command containing updated semester details
     * @return ResponseEntity containing the updated semester details
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SemesterDto> update(
            @PathVariable String id,
            @RequestBody UpdateSemesterCommand command) {
        command.setId(id);

        return ResponseEntity.ok(pipeline.send(command));
    }

    /**
     * Delete a semester by its ID.
     *
     * @param id ID of the semester to delete
     * @return ResponseEntity containing the details of the deleted semester
     */
    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SemesterDto> delete(@PathVariable String id) {
        var command = DeleteSemesterCommand.builder().id(id).build();

        return ResponseEntity.ok(pipeline.send(command));
    }
}