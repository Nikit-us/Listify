package com.tech.listify.controller;

import com.tech.listify.dto.ApiErrorResponse;
import com.tech.listify.dto.categorydto.CategoryCreateDto;
import com.tech.listify.dto.categorydto.CategoryDto;
import com.tech.listify.dto.categorydto.CategoryTreeDto;
import com.tech.listify.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "API для получения и управления категориями объявлений")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Получить плоский список всех категорий",
            description = "Возвращает полный список доступных категорий объявлений без иерархии. Доступно всем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список категорий успешно получен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CategoryDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        log.debug("Request to get all categories");
        List<CategoryDto> categories = categoryService.findAllCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Получить категорию по ID",
            description = "Возвращает информацию о конкретной категории по ее уникальному идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Категория не найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(
            @Parameter(description = "ID категории", required = true, example = "1")
            @PathVariable Integer id) {
        log.debug("Request to get category by ID: {}", id);
        CategoryDto category = categoryService.findAllCategories().stream()
                .filter(c -> c.id().equals(id)).findFirst().orElseThrow();
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "Создать одну или несколько категорий",
            description = "Создает новые категории. Можно создавать как корневые категории, так и подкатегории, " +
                    "указав `parentCategoryId`. Требуется роль ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Категории успешно созданы",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryDto.class)))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (недостаточно прав)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Родительская категория с указанным ID не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Категория с таким названием уже существует",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<List<CategoryDto>> createCategories(
            @RequestBody(description = "Список объектов для создания новых категорий.", required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryCreateDto.class))))
            @Valid @org.springframework.web.bind.annotation.RequestBody List<CategoryCreateDto> createDtos) {
        if (createDtos == null || createDtos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Список для создания не может быть пустым.");
        }
        List<CategoryDto> createdCategories = categoryService.createCategories(createDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategories);
    }

    @Operation(summary = "Получить иерархическое дерево всех категорий",
            description = "Возвращает категории в виде дерева, где у каждого элемента есть список дочерних. " +
                    "Идеально подходит для построения меню категорий. Доступно всем.")
    @ApiResponse(responseCode = "200", description = "Дерево категорий успешно получено",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = CategoryTreeDto.class))))
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryTreeDto>> getCategoriesAsTree() {
        log.debug("Request to get categories as a tree");
        List<CategoryTreeDto> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(categoryTree);
    }
}