package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeUserDto;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeVersionDto;
import com.hcmus.mentor.backend.controller.usecase.grade.creategrade.CreateGradeCommand;
import com.hcmus.mentor.backend.controller.usecase.grade.deletegrade.DeleteGradeCommand;
import com.hcmus.mentor.backend.controller.usecase.grade.deleteversion.DeleteVersionCommand;
import com.hcmus.mentor.backend.controller.usecase.grade.getgrade.SearchGradeQuery;
import com.hcmus.mentor.backend.controller.usecase.grade.getgrade.SearchGradeResult;
import com.hcmus.mentor.backend.controller.usecase.grade.getgradebyid.GetGradeByIdQuery;
import com.hcmus.mentor.backend.controller.usecase.grade.getshareinfobyuserid.GetShareInfoByUserQuery;
import com.hcmus.mentor.backend.controller.usecase.grade.getversionsbystudentid.GetVersionsByStudentIdCommand;
import com.hcmus.mentor.backend.controller.usecase.grade.importgrade.ImportGradeCommand;
import com.hcmus.mentor.backend.controller.usecase.grade.sharegrade.ShareGradeCommand;
import com.hcmus.mentor.backend.controller.usecase.grade.updategrade.UpdateGradeCommand;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Grade controller.
 */
@Tag(name = "grades")
@RestController
@RequestMapping("api/grades")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class GradeController {

    private final Pipeline pipeline;

    /**
     * Search grade.
     *
     * @param query Search criteria for grades.
     * @return ResponseEntity containing the search results.
     */
    @GetMapping("")
    public ResponseEntity<SearchGradeResult> searchGrade(SearchGradeQuery query) {
        var result = pipeline.send(query);

        return ResponseEntity.ok(result);
    }

    /**
     * Retrieve a grade by its ID
     *
     * @param id ID of the grade to retrieve
     * @return ResponseEntity containing the grade details
     */
    @GetMapping("{id}")
    public ResponseEntity<GradeDto> getGradeById(@PathVariable String id) {
        var query = GetGradeByIdQuery.builder().id(id).build();

        return ResponseEntity.ok(pipeline.send(query));
    }

    /**
     * Create a new grade
     *
     * @param command Command containing grade details for creation
     * @return ResponseEntity containing the created grade details
     */
    @PostMapping("")
    public ResponseEntity<GradeDto> create(@RequestBody CreateGradeCommand command) {
        return ResponseEntity.ok(pipeline.send(command));
    }

    /**
     * Update an existing grade
     *
     * @param id      ID of the grade to update
     * @param command Command containing updated grade details
     * @return ÎResponseEntity containing the updated grade details
     */
    @PatchMapping("{id}")
    public ResponseEntity<GradeDto> updateGrade(@PathVariable String id, @RequestBody UpdateGradeCommand command) {
        command.setId(id);

        return ResponseEntity.ok(pipeline.send(command));
    }

    /**
     * Delete a grade by its ID
     *
     * @param id ID of the grade to delete
     * @return ResponseEntity containing the details of the deleted grade
     */
    @DeleteMapping("{id}")
    public ResponseEntity<GradeDto> deleteGrade(@PathVariable String id) {
        var command = DeleteGradeCommand.builder().id(id).build();

        return ResponseEntity.ok(pipeline.send(command));
    }

    @PostMapping("/share")
    public ResponseEntity<GradeUserDto> shareGrade(@RequestBody ShareGradeCommand command) {
        return ResponseEntity.ok(pipeline.send(command));
    }

    @GetMapping("/share/{userId}")
    public ResponseEntity<GradeUserDto> getShareInfo(@PathVariable String userId) {
        return ResponseEntity.ok(pipeline.send(GetShareInfoByUserQuery.builder().userId(userId).build()));
    }

    @PostMapping(value = "/import/{userId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<List<GradeDto>> importGrade(@RequestParam("file") MultipartFile file, @PathVariable String userId) {
        var command = ImportGradeCommand.builder().file(file).userId(userId).build();

        return ResponseEntity.ok(pipeline.send(command));
    }

    @GetMapping("versions/student/{studentId}")
    public ResponseEntity<List<GradeVersionDto>> getStudentVersions(@PathVariable String studentId) {
        return ResponseEntity.ok(pipeline.send(GetVersionsByStudentIdCommand.builder().studentId(studentId).build()));
    }

    @DeleteMapping("versions/{versionId}")
    public ResponseEntity<Void> deleteVersion(@PathVariable String versionId) {
        pipeline.send(DeleteVersionCommand.builder().versionId(versionId).build());

        return ResponseEntity.ok().build();
    }
}