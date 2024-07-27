package com.hcmus.mentor.backend.controller.usecase.auditrecord;

import com.hcmus.mentor.backend.controller.usecase.auditrecord.commond.AuditRecordDto;
import com.hcmus.mentor.backend.controller.usecase.common.mapper.MapperConverter;
import com.hcmus.mentor.backend.domain.AuditRecord;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class AuditRecordMapper {

    public AuditRecordMapper(ModelMapper modelMapper) {
        modelMapper.emptyTypeMap(AuditRecord.class, AuditRecordDto.class).addMappings(mapper -> {
            mapper.using(MapperConverter.toLocalDateTime).map(AuditRecord::getCreatedDate, AuditRecordDto::setCreatedDate);
            mapper.using(MapperConverter.toLocalDateTime).map(AuditRecord::getUpdatedDate, AuditRecordDto::setUpdatedDate);
        }).implicitMappings();
    }
}