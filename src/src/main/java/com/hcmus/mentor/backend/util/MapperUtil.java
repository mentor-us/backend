package com.hcmus.mentor.backend.util;

import com.hcmus.mentor.backend.domain.BaseDomain;
import org.modelmapper.Converter;

import java.util.Collections;
import java.util.List;

public class MapperUtil {

    public static final Converter<List<BaseDomain>, List<String>> mapIdDomainConverter = ctx -> {
        if (ctx.getSource() == null) {
            return Collections.emptyList();
        }

        return ctx.getSource().stream().map(BaseDomain::getId).toList();
    };

    private MapperUtil() {
    }
}
