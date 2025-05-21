package com.tech.listify.service;

import com.tech.listify.dto.categoryDto.CategoryDto;
import com.tech.listify.model.Category;

import java.util.List;

public interface CategoryService {
    Category findCategoryById(Integer Id);
    List<CategoryDto> findAllCategories();
}
