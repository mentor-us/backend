package com.hcmus.mentor.backend.controller.usecase.schoolyear.search;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.schoolyear.common.SchoolYearDto;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.SchoolYearService;
import com.hcmus.mentor.backend.util.MappingUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchSchoolYearQueryHandler implements Command.Handler<SearchSchoolYearQuery, SearchSchoolYearResult> {

    private final Logger logger = LoggerFactory.getLogger(SearchSchoolYearQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final SchoolYearService schoolYearService;

    @Override
    public SearchSchoolYearResult handle(SearchSchoolYearQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var schoolYearPage = schoolYearService.search(query);
        var data = schoolYearPage.getContent().stream()
                .map(s -> modelMapper.map(s, SchoolYearDto.class))
                .toList();

        var result = new SearchSchoolYearResult();
        result.setData(data);
        MappingUtil.mapPageQueryMetadata(schoolYearPage, result);

        logger.debug("User {} search school year with query: {}", currentUserId, query);

        return result;
    }
}
