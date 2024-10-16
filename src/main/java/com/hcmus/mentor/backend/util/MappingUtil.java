package com.hcmus.mentor.backend.util;

import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryResult;
import org.springframework.data.domain.Page;

public class MappingUtil {

    private MappingUtil() {
    }

    public static void mapPageQueryMetadata(Page<?> page, PageQueryResult<?> result) {
        result.setPage(page.getNumber());
        result.setPageSize(page.getSize());
        result.setTotalPages(page.getTotalPages());
        result.setTotalCounts(page.getTotalElements());
        result.setFirst(page.isFirst());
        result.setLast(page.isLast());
        result.setTotalPages(page.getTotalPages());
    }
}