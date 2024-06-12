package com.hcmus.mentor.backend.controller.usecase.semester.search;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.semester.common.SemesterDto;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.SemesterService;
import com.hcmus.mentor.backend.util.MappingUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchSemesterQueryHandler implements Command.Handler<SearchSemesterQuery, SearchSemesterResult> {

    private final Logger logger = LoggerFactory.getLogger(SearchSemesterQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final SemesterService semesterService;

    @Override
    public SearchSemesterResult handle(SearchSemesterQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var semesterPage = semesterService.search(query);
        var data = semesterPage.getContent().stream()
                .map(s -> modelMapper.map(s, SemesterDto.class))
                .toList();

        var result = new SearchSemesterResult();
        result.setData(data);
        MappingUtil.mapPageQueryMetadata(semesterPage, result);

        logger.debug("User {} search semester with query: {}", currentUserId, query);

        return result;
    }
}
