package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.payload.request.note.CreateNoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.note.ShareNoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.note.UpdateNoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.note.UpdateNoteUserRequest;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDto;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;
import com.hcmus.mentor.backend.controller.usecase.note.createnote.CreateNoteCommand;
import com.hcmus.mentor.backend.domainservice.NoteDomainService;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "notes")
@RestController
@RequestMapping("api/notes")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class NoteController {

    private final Pipeline pipeline;
    private final NoteDomainService noteDomainService;
    private final ModelMapper modelMapper;

    @GetMapping("{id}")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> getNoteById(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedInUser,
            @PathVariable String id) {
        throw new NotImplementedException();
    }

    @GetMapping("/users")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<List<NoteUserProfile>> getAllUserCanAccessNotes(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedInUser) {
        return ResponseEntity.ok(noteDomainService.getAllUsers(loggedInUser.getId()));
    }

    @GetMapping("user/{userId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<List<NoteDto>> getAllNotesByUserId(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedInUser,
            @PathVariable String userId) {
        throw new NotImplementedException();
    }

    @PostMapping("")
    @ApiResponse(responseCode = "201", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> createNote(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedInUser,
            @RequestBody CreateNoteRequest request) {
        var command = modelMapper.map(request, CreateNoteCommand.class);
        command.setCreatorId(loggedInUser.getId());
       NoteDetailDto abc = pipeline.send(command);
        return ResponseEntity.ok(abc);
    }

    @PostMapping("{id}/share")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> shareNoteToUser(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedInUser,
            @PathVariable String id,
            @RequestParam ShareNoteRequest request) {
        throw new NotImplementedException();
    }

    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> updateNote(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedInUser,
            @PathVariable String id,
            @RequestBody UpdateNoteRequest request) {
        throw new NotImplementedException();
    }

    @PutMapping("{id}/users")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> publishNote(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedInUser,
            @PathVariable String id,
            @Valid @RequestBody UpdateNoteUserRequest request) {
        throw new NotImplementedException();
    }

    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "204", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<Void> deleteNoteById(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedInUser,
            @PathVariable String id) {
        throw new NotImplementedException();
    }
}