package com.hcmus.mentor.backend.controller.usecase.auditrecord.search;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SearchAuditRecordQuery extends PageQueryFilter implements Command<SearchAuditRecordResult> {
}
