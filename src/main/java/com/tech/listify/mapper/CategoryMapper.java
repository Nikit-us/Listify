package com.tech.listify.mapper;

import com.tech.listify.dto.categorydto.CategoryCreateDto;
import com.tech.listify.dto.categorydto.CategoryDto;
import com.tech.listify.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category entity);

    List<CategoryDto> toDtoList(List<Category> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    @Mapping(target = "advertisements", ignore = true)
    Category toEntity(CategoryCreateDto dto);

    List<Category> toEntityList(List<CategoryCreateDto> dtoList);
}
