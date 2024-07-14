package com.hcmus.mentor.backend.controller.usecase.auditrecord.commond;

import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecordDto {
    private String id;
    private String entityId;
    private ActionType action;
    private DomainType domain;
    private String detail;
    private ShortProfile user;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}