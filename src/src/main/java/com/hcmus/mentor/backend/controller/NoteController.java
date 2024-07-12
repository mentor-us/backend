package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.payload.request.note.CreateNoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.note.GetNotesByUserRequest;
import com.hcmus.mentor.backend.controller.payload.request.note.ShareNoteRequest;
import com.hcmus.mentor.backend.controller.payload.request.note.UpdateNoteRequest;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteDetailDto;
import com.hcmus.mentor.backend.controller.usecase.note.createnote.CreateNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.deletenote.DeleteNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.getnotedetailbyid.GetNoteDetailByIdQuery;
import com.hcmus.mentor.backend.controller.usecase.note.getnotesbyuserid.GetNoteResult;
import com.hcmus.mentor.backend.controller.usecase.note.getnotesbyuserid.GetNotesByUserIdQuery;
import com.hcmus.mentor.backend.controller.usecase.note.searchuserhasnotebyviewer.SearchUserHasNoteByViewerQuery;
import com.hcmus.mentor.backend.controller.usecase.note.searchuserhasnotebyviewer.SearchUserHasNoteByViewerResult;
import com.hcmus.mentor.backend.controller.usecase.note.sharenote.ShareNoteCommand;
import com.hcmus.mentor.backend.controller.usecase.note.updatenote.UpdateNoteCommand;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Note controller.
 */
@Tag(name = "notes")
@RestController
@RequestMapping("api/notes")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class NoteController {

    private final Pipeline pipeline;
    private final ModelMapper modelMapper;

    /**
     * Retrieves a note by its ID.
     *
     * @param noteId The ID of the note to retrieve.
     * @return ResponseEntity containing the NoteDetailDto.
     */
    @GetMapping("{noteId}")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> getById(@PathVariable String noteId) {
        return ResponseEntity.ok(pipeline.send(GetNoteDetailByIdQuery.builder().noteId(noteId).build()));
    }

    /**
     * Searches for users who have notes visible to the viewer.
     *
     * @param query The search query.
     * @return ResponseEntity containing the SearchUserHasNoteByViewerResult.
     */
    @GetMapping("/users")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<SearchUserHasNoteByViewerResult> searchUserHasNoteByViewer(SearchUserHasNoteByViewerQuery query) {
        return ResponseEntity.ok(pipeline.send(query));
    }

    /**
     * Retrieves all notes belonging to a specific user.
     *
     * @param userId  The ID of the user whose notes are to be retrieved.
     * @param request Additional request parameters.
     * @return ResponseEntity containing the GetNoteResult.
     */
    @GetMapping("user/{userId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<GetNoteResult> getAllNotesByUserId(@PathVariable String userId, GetNotesByUserRequest request) {
        var command = modelMapper.map(request, GetNotesByUserIdQuery.class);
        command.setUserId(userId);
        return ResponseEntity.ok(pipeline.send(command));
    }

    /**
     * Creates a new note.
     *
     * @param request The request body containing note creation details.
     * @return ResponseEntity containing the created NoteDetailDto.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "201", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> create(@Valid @RequestBody CreateNoteRequest request) {
        var command = modelMapper.map(request, CreateNoteCommand.class);
        return ResponseEntity.ok(pipeline.send(command));
    }

    /**
     * Shares a note with another user.
     *
     * @param id      The ID of the note to be shared.
     * @param request The request body containing share details.
     * @return ResponseEntity containing the updated NoteDetailDto after sharing.
     */
    @PostMapping("{id}/share")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> shareNoteToUser(@PathVariable String id, @RequestBody ShareNoteRequest request) {
        var command = modelMapper.map(request, ShareNoteCommand.class);
        command.setNoteId(id);
        return ResponseEntity.ok(pipeline.send(command));
    }

    /**
     * Updates an existing note.
     *
     * @param id      The ID of the note to update.
     * @param request The request body containing update details.
     * @return ResponseEntity containing the updated NoteDetailDto.
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<NoteDetailDto> update(@PathVariable String id, @RequestBody UpdateNoteRequest request) {
        var command = modelMapper.map(request, UpdateNoteCommand.class);
        command.setNoteId(id);
        return ResponseEntity.ok(pipeline.send(command));
    }

    /**
     * Deletes a note by its ID.
     *
     * @param id The ID of the note to delete.
     * @return ResponseEntity with no content (204).
     */
    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "204", description = "Success")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        return ResponseEntity.ok(pipeline.send(DeleteNoteCommand.builder().noteId(id).build()));
    }
}