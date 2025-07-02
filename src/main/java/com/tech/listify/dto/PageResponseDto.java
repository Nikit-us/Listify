package com.tech.listify.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDto<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponseDto<T>fromPage(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
