package com.hcmus.mentor.backend.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.usecase.auditrecord.search.SearchAuditRecordQuery;
import com.hcmus.mentor.backend.controller.usecase.auditrecord.search.SearchAuditRecordResult;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Audit log controller.
 */
@Tag(name = "audits")
@RestController
@RequestMapping("api/audits")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class AuditController {

    private final Pipeline pipeline;

    @GetMapping("")
    public ResponseEntity<SearchAuditRecordResult> search(SearchAuditRecordQuery query) {
        var result = pipeline.send(query);

        return ResponseEntity.ok(result);
    }

    @GetMapping("{id}")
    public ResponseEntity get() {
        return null;
    }
}
