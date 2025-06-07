package com.tech.listify.service.impl;

import com.tech.listify.dto.categorydto.CategoryCreateDto;
import com.tech.listify.dto.categorydto.CategoryDto;
import com.tech.listify.exception.ResourceAlreadyExistsException;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.mapper.CategoryMapper;
import com.tech.listify.model.Category;
import com.tech.listify.repository.CategoryRepository;
import com.tech.listify.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryService self;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               CategoryMapper categoryMapper,
                               @Lazy CategoryService self) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.self = self;
    }

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

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true) // Очищаем весь кеш категорий
    public List<CategoryDto> createCategories(List<CategoryCreateDto> createDtos) {
        log.info("Attempting to create {} new categories.", createDtos.size());

        // Проверка на дубликаты имен в запросе
        List<String> namesToCreate = createDtos.stream()
                .map(dto -> dto.name().toLowerCase())
                .toList();
        List<Category> existingCategories = categoryRepository.findByNameInIgnoreCase(namesToCreate);
        if (!existingCategories.isEmpty()) {
            String existingNames = existingCategories.stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(", "));
            throw new ResourceAlreadyExistsException("Категории уже существуют: " + existingNames);
        }

        // Подготовка сущностей к сохранению
        List<Category> categoriesToSave = new ArrayList<>();
        for (CategoryCreateDto dto : createDtos) {
            Category newCategory = categoryMapper.toEntity(dto);
            // Если указан ID родителя, находим и устанавливаем его
            if (dto.parentCategoryId() != null) {
                Category parent = self.findCategoryById(dto.parentCategoryId());
                newCategory.setParentCategory(parent);
            }
            categoriesToSave.add(newCategory);
        }

        // Сохраняем все подготовленные сущности
        List<Category> savedCategories = categoryRepository.saveAll(categoriesToSave);

        log.info("Successfully created {} categories.", savedCategories.size());
        return categoryMapper.toDtoList(savedCategories);
    }
}
