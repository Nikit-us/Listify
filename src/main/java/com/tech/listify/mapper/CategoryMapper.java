package com.tech.listify.mapper;

import com.tech.listify.dto.categoryDto.CategoryDto;
import com.tech.listify.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category entity);
}
