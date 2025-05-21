package com.tech.listify.mapper.impl;

import com.tech.listify.dto.categoryDto.CategoryCreateDto;
import com.tech.listify.dto.categoryDto.CategoryDto;
import com.tech.listify.mapper.CategoryMapper;
import com.tech.listify.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapperImpl implements CategoryMapper {
    @Override
    public Category toEntity(CategoryCreateDto dto) {
        return null;
    }

    @Override
    public CategoryDto toDto(Category entity) {
        if(entity == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }
}
