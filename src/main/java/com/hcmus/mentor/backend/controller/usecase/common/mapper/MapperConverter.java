package com.hcmus.mentor.backend.controller.usecase.common.mapper;

import com.hcmus.mentor.backend.domain.BaseDomain;
import org.modelmapper.Converter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MapperConverter {

    private MapperConverter() {
    }

    public static final Converter<List<BaseDomain>, List<String>> mapIdConverter = ctx -> {
        if (ctx.getSource() == null) {
            return Collections.emptyList();
        }
        return ctx.getSource().stream().map(BaseDomain::getId).toList();
    };

    public static final Converter<Date, LocalDateTime> toLocalDateTime = ctx -> {
        if (ctx.getSource() == null) {
            return null;
        }
        return new Date(ctx.getSource().getTime()).toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
    };
}