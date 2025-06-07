package com.tech.listify.controller;

import com.tech.listify.dto.categorydto.CategoryCreateDto;
import com.tech.listify.dto.categorydto.CategoryDto;
import com.tech.listify.mapper.CategoryMapper;
import com.tech.listify.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "API для получения информации о категориях объявлений")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @Operation(summary = "Получить список всех категорий",
            description = "Возвращает полный список доступных категорий объявлений. Доступно всем.")
    @ApiResponse(responseCode = "200", description = "Список категорий успешно получен",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = CategoryDto.class))))
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        log.debug("Request to get all categories");
        List<CategoryDto> categories = categoryService.findAllCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Получить категорию по ID", description = "Возвращает информацию о конкретной категории.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Категория не найдена", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(
            @Parameter(description = "ID категории", required = true, example = "1") @PathVariable Integer id) {
        log.debug("Request to get category by ID: {}", id);
        CategoryDto category = categoryMapper.toDto(categoryService.findCategoryById(id));
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "Создать одну или несколько категорий (только для ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Категории успешно созданы"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Родительская категория не найдена"),
            @ApiResponse(responseCode = "409", description = "Категория уже существует")
    })
    @PostMapping
    public ResponseEntity<List<CategoryDto>> createCategories(@Valid @RequestBody List<CategoryCreateDto> createDtos) {
        if (createDtos == null || createDtos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Список для создания не может быть пустым.");
        }
        List<CategoryDto> createdCategories = categoryService.createCategories(createDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategories);
    }
}