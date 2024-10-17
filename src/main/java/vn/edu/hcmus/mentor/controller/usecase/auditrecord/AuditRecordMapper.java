package vn.edu.hcmus.mentor.controller.usecase.auditrecord;

import vn.edu.hcmus.mentor.controller.usecase.auditrecord.commond.AuditRecordDto;
import vn.edu.hcmus.mentor.controller.usecase.common.mapper.MapperConverter;
import vn.edu.hcmus.mentor.domain.AuditRecord;
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