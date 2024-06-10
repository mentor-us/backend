package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.payload.request.note.*;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDto;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;
import com.hcmus.mentor.backend.controller.usecase.note.createnote.CreateNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.deletenote.DeleteNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.getnotedetailbyid.GetNoteDetailByIdQuery;
import com.hcmus.mentor.backend.controller.usecase.note.getnotesbyuserid.GetNotesByUserIdQuery;
import com.hcmus.mentor.backend.controller.usecase.note.sharenote.ShareNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.updatenote.UpdateNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.updatenoteuser.UpdateNoteUserCommand;
import com.hcmus.mentor.backend.domainservice.NoteDomainService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
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

    @GetMapping("{noteId}")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> getById(@PathVariable String noteId) {
        return ResponseEntity.ok(pipeline.send(GetNoteDetailByIdQuery.builder().noteId(noteId).build()));
    }

    @GetMapping("/users")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<List<NoteUserProfile>> getAllUserCanAccessNotes() {
        return ResponseEntity.ok(noteDomainService.getAllUsers());
    }

    @GetMapping("user/{userId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<Page<NoteDto>> getAllNotesByUserId(@PathVariable String userId, @RequestBody GetNotesByUserRequest request) {
        var command = modelMapper.map(request, GetNotesByUserIdQuery.class);
        command.setUserId(userId);
        return ResponseEntity.ok(pipeline.send(command));
    }

    @PostMapping("")
    @ApiResponse(responseCode = "201", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> create(@Valid @RequestBody CreateNoteRequest request) {
        var command = modelMapper.map(request, CreateNoteCommand.class);
        return ResponseEntity.ok(pipeline.send(command));
    }

    @PostMapping("{id}/share")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> shareNoteToUser(@PathVariable String id, @RequestBody ShareNoteRequest request) {
        var command = modelMapper.map(request, ShareNoteCommand.class);
        command.setNoteId(id);
        return ResponseEntity.ok(pipeline.send(command));
    }

    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> update(@PathVariable String id, @RequestBody UpdateNoteRequest request) {
        var command = modelMapper.map(request, UpdateNoteCommand.class);
        command.setNoteId(id);
        return ResponseEntity.ok(pipeline.send(command));
    }

    @PutMapping("{id}/users")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> publishNote(@PathVariable String id, @Valid @RequestBody UpdateNoteUserRequest request) {
        var command = modelMapper.map(request, UpdateNoteUserCommand.class);
        command.setNoteId(id);
        return ResponseEntity.ok(pipeline.send(command));
    }

    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "204", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
       return ResponseEntity.ok(pipeline.send(new DeleteNoteCommand(id)));
    }
}