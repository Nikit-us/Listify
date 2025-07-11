package com.tech.listify.service;

import com.tech.listify.dto.categorydto.CategoryCreateDto;
import com.tech.listify.dto.categorydto.CategoryDto;
import com.tech.listify.dto.categorydto.CategoryTreeDto;
import com.tech.listify.model.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto findCategoryById(Integer id);

    List<CategoryDto> findAllCategories();

    List<CategoryDto> createCategories(List<CategoryCreateDto> createDtos);

    List<CategoryTreeDto> getCategoryTree();
}
