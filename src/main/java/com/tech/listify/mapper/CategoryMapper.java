package com.tech.listify.mapper;

import com.tech.listify.dto.categoryDto.CategoryCreateDto;
import com.tech.listify.dto.categoryDto.CategoryDto;
import com.tech.listify.model.Category;

public interface CategoryMapper {
    Category toEntity(CategoryCreateDto dto);
    CategoryDto toDto(Category entity);
}
