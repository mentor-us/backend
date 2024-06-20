package com.hcmus.mentor.backend.controller.usecase.note.searchuserhasnotebyviewer;

import an.awesome.pipelinr.Command;
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
        var noteUserProfilePage = userRepository.findAllAccessNote(currentUserId, command.getQuery(), PageRequest.of(command.getPage(), command.getPageSize()) );
        var result = new SearchUserHasNoteByViewerResult();
        result.setData(noteUserProfilePage.getContent());
        MappingUtil.mapPageQueryMetadata(noteUserProfilePage, result);
        return result;
    }
}