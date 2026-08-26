package com.essay.agent.model.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private Long total;
    private Integer page;
    private Integer size;
    private List<T> records;

    public PageResult(Long total, Integer page, Integer size, List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.records = records;
    }

    public static <T> PageResult<T> of(org.springframework.data.domain.Page<T> page) {
        return new PageResult<>(page.getTotalElements(), page.getNumber(), page.getSize(), page.getContent());
    }
}