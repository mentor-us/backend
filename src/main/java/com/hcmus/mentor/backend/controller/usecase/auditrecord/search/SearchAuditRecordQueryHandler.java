package com.hcmus.mentor.backend.controller.usecase.auditrecord.search;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.auditrecord.commond.AuditRecordDto;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.util.MappingUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchAuditRecordQueryHandler implements Command.Handler<SearchAuditRecordQuery, SearchAuditRecordResult> {

    private final Logger logger = LoggerFactory.getLogger(SearchAuditRecordQueryHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final AuditRecordService auditRecordService;

    @Override
    public SearchAuditRecordResult handle(SearchAuditRecordQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var auditRecordPage = auditRecordService.search(query);
        var data = auditRecordPage.getContent().stream()
                .map(auditRecord -> modelMapper.map(auditRecord, AuditRecordDto.class))
                .toList();

        var result = new SearchAuditRecordResult();
        result.setData(data);
        MappingUtil.mapPageQueryMetadata(auditRecordPage, result);

        logger.debug("User {} search grade with query: {}", currentUserId, query);

        return result;
    }
}
