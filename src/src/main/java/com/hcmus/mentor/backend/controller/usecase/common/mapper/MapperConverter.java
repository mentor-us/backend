package com.hcmus.mentor.backend.controller.usecase.common.mapper;

import com.hcmus.mentor.backend.domain.BaseDomain;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;


@Data
@RequiredArgsConstructor
public class MapperConverter {
    private ModelMapper modelMapper;

    public static Converter<List<BaseDomain>, List<String>> mapIdConverter() {
        return ctx -> {
            if (ctx.getSource() == null) {
                return Collections.emptyList();
            }
            return ctx.getSource().stream().map(BaseDomain::getId).toList();
        };
    }

    public static Converter<Date, LocalDateTime> mapDateConverter() {
        return ctx -> {
            if (ctx.getSource() == null) {
                return null;
            }
            return new Date(ctx.getSource().getTime()).toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
        };
    }
}