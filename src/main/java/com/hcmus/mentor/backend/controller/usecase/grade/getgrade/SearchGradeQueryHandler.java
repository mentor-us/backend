package com.hcmus.mentor.backend.controller.usecase.grade.getgrade;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeDto;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.GradeService;
import com.hcmus.mentor.backend.util.MappingUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchGradeQueryHandler implements Command.Handler<SearchGradeQuery, SearchGradeResult> {

    private final Logger logger = LoggerFactory.getLogger(SearchGradeQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final GradeService gradeService;

    @Override
    public SearchGradeResult handle(SearchGradeQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var gradePage = gradeService.searchGrade(query);
        var data = gradePage.getContent().stream()
                .map(grade -> modelMapper.map(grade, GradeDto.class))
                .toList();

        var result = new SearchGradeResult();
        result.setData(data);
        MappingUtil.mapPageQueryMetadata(gradePage, result);

        logger.debug("User {} search grade with query: {}", currentUserId, query);

        return result;
    }
}