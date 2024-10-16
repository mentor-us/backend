package com.hcmus.mentor.backend.controller.usecase.common.pagination;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class PageQueryResult<T> {

    protected int page;
    protected int pageSize;
    protected int totalPages;
    protected long totalCounts;
    protected boolean first;
    protected boolean last;
    protected List<T> data;
}
