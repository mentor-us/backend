package com.hcmus.mentor.backend.controller.usecase.common.pagination;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class PageQueryFilter {

    @JsonProperty("page")
    protected int page = 0;

    @JsonProperty("pageSize")
    protected int pageSize = 25;

    public void setSize(int size) {
        this.pageSize = size;
    }
}
