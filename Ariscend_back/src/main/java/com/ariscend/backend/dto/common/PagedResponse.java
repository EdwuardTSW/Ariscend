package com.ariscend.backend.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static <T> PagedResponse<T> from(Page<T> source) {
        PagedResponse<T> response = new PagedResponse<>();
        response.content = source.getContent();
        response.page = source.getNumber();
        response.size = source.getSize();
        response.totalElements = source.getTotalElements();
        response.totalPages = source.getTotalPages();
        response.first = source.isFirst();
        response.last = source.isLast();
        return response;
    }

    public List<T> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }
}
