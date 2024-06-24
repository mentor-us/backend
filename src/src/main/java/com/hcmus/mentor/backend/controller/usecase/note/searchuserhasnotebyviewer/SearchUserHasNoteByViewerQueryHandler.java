package com.hcmus.mentor.backend.controller.usecase.note.searchuserhasnotebyviewer;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.note.common.NoteUserProfile;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.util.MappingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchUserHasNoteByViewerQueryHandler implements Command.Handler<SearchUserHasNoteByViewerQuery, SearchUserHasNoteByViewerResult> {
    private final UserRepository userRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public SearchUserHasNoteByViewerResult handle(SearchUserHasNoteByViewerQuery command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var noteUserProfileProjectionPage = userRepository.findAllUsersHasNoteAccess(
                currentUserId, command.getQuery(), PageRequest.of(command.getPage(), command.getPageSize()));
        var noteUserProfiles = noteUserProfileProjectionPage.getContent().stream().map(noteUserProfileProjection -> NoteUserProfile.builder()
                .id(noteUserProfileProjection.getId())
                .name(noteUserProfileProjection.getName())
                .email(noteUserProfileProjection.getEmail())
                .imageUrl(noteUserProfileProjection.getImageUrl())
                .totalNotes(noteUserProfileProjection.getTotalNotes())
                .build()).toList();

        var result = new SearchUserHasNoteByViewerResult();
        result.setData(noteUserProfiles);
        MappingUtil.mapPageQueryMetadata(noteUserProfileProjectionPage, result);

        return result;
    }
}