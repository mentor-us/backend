package com.hcmus.mentor.backend.controller.usecase.auditrecord.commond;

import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
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
    private String type;
    private String detail;
    private ShortProfile user;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}