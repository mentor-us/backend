package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.usecase.course.common.CourseDto;
import com.hcmus.mentor.backend.controller.usecase.course.create.CreateCourseCommand;
import com.hcmus.mentor.backend.controller.usecase.course.delete.DeleteCourseCommand;
import com.hcmus.mentor.backend.controller.usecase.course.getbyid.GetCourseByIdQuery;
import com.hcmus.mentor.backend.controller.usecase.course.search.SearchCourseQuery;
import com.hcmus.mentor.backend.controller.usecase.course.search.SearchCourseResult;
import com.hcmus.mentor.backend.controller.usecase.course.update.UpdateCourseCommand;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "courses")
@RestController
@RequestMapping("api/courses")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class CourseController {

    private final Pipeline pipeline;

    @GetMapping("")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<SearchCourseResult> search(SearchCourseQuery query) {
        var result = pipeline.send(query);

        return ResponseEntity.ok(result);
    }

    @GetMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<CourseDto> getById(
            @PathVariable String id) {
        var query = GetCourseByIdQuery.builder().id(id).build();

        return ResponseEntity.ok(pipeline.send(query));
    }

    @PostMapping("")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<CourseDto> create(
            @RequestBody CreateCourseCommand command) {
        return ResponseEntity.ok(pipeline.send(command));
    }

    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<CourseDto> update(
            @PathVariable String id,
            @RequestBody UpdateCourseCommand command) {
        command.setId(id);

        return ResponseEntity.ok(pipeline.send(command));
    }

    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<CourseDto> delete(@PathVariable String id) {
        var command = DeleteCourseCommand.builder().id(id).build();

        return ResponseEntity.ok(pipeline.send(command));
    }
}
