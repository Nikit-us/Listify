package com.tech.listify.service.impl;

import com.tech.listify.dto.categoryDto.CategoryDto;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.mapper.CategoryMapper;
import com.tech.listify.model.Category;
import com.tech.listify.repository.CategoryRepository;
import com.tech.listify.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Cacheable(cacheNames = "categories", key = "#id")
    public Category findCategoryById(Integer id) {
        log.debug("Fetching category with ID: {} from repository", id);

        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категория с ID " + id + " не найдена."));
    }

    @Override
    @Cacheable(cacheNames = "categories")
    public List<CategoryDto> findAllCategories() {
        log.debug("Fetching all categories from repository");
        return categoryRepository.findAll().stream().map(categoryMapper::toDto).toList();
    }
}
