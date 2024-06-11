package com.hcmus.mentor.backend.controller.usecase.course.search;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.course.common.CourseDto;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.impl.CourseService;
import com.hcmus.mentor.backend.util.MappingUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchCourseQueryHandler implements Command.Handler<SearchCourseQuery, SearchCourseResult> {

    private final Logger logger = LoggerFactory.getLogger(SearchCourseQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final CourseService courseService;

    @Override
    public SearchCourseResult handle(SearchCourseQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var coursePage = courseService.search(query);
        var data = coursePage.getContent().stream()
                .map(c -> modelMapper.map(c, CourseDto.class))
                .toList();

        var result = new SearchCourseResult();
        result.setData(data);
        MappingUtil.mapPageQueryMetadata(coursePage, result);

        logger.debug("User {} search course with query: {}", currentUserId, query);

        return result;
    }
}
