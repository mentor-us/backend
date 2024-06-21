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

/**
 * School years controller.
 */
@Tag(name = "years")
@RestController
@RequestMapping("api/years")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class SchoolYearController {

    private final Pipeline pipeline;

    /**
     * Search for school years based on criteria.
     *
     * @param query Search criteria for school years
     * @return ResponseEntity containing the search results
     */

    @GetMapping("")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SearchSchoolYearResult> search(SearchSchoolYearQuery query) {
        var result = pipeline.send(query);

        return ResponseEntity.ok(result);
    }

    /**
     * Retrieve a school year by its ID.
     *
     * @param id ID of the school year to retrieve
     * @return ResponseEntity containing the school year details
     */
    @GetMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SchoolYearDto> getById(@PathVariable String id) {
        var query = GetSchoolYearByIdQuery.builder().id(id).build();

        return ResponseEntity.ok(pipeline.send(query));
    }

    /**
     * Create a new school year.
     *
     * @param command Command containing school year details for creation
     * @return ResponseEntity containing the created school year details
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SchoolYearDto> create(@RequestBody CreateSchoolYearCommand command) {
        return ResponseEntity.ok(pipeline.send(command));
    }

    /**
     * Update an existing school year.
     *
     * @param id      ID of the school year to update
     * @param command Command containing updated school year details
     * @return ResponseEntity containing the updated school year details
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SchoolYearDto> update(@PathVariable String id, @RequestBody UpdateSchoolYearCommand command) {
        command.setId(id);

        return ResponseEntity.ok(pipeline.send(command));
    }

    /**
     * Delete a school year by its ID.
     *
     * @param id ID of the school year to delete
     * @return ResponseEntity containing the details of the deleted school year
     */
    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SchoolYearDto> delete(@PathVariable String id) {
        var command = DeleteSchoolYearCommand.builder().id(id).build();

        return ResponseEntity.ok(pipeline.send(command));
    }
}