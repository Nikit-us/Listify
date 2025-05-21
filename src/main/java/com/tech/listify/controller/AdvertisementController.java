package com.tech.listify.controller;

import com.tech.listify.dto.advertisementDto.*;
import com.tech.listify.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/ads")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Advertisements", description = "API для управления объявлениями")
public class AdvertisementController {
    private final AdvertisementService advertisementService;

    @Operation(summary = "Создать новое объявление",
            description = "Создает новое объявление с текстовыми данными и опциональными изображениями. Требуется аутентификация.",
            security = @SecurityRequirement(name = "bearerAuth")) // Указываем, что эндпоинт защищен
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Объявление успешно создано",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdvertisementDetailDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или некорректный запрос", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Связанный ресурс (категория/город) не найден", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера (например, ошибка сохранения файла)", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping
    public ResponseEntity<AdvertisementDetailDto> createAdvertisement(@Valid @RequestPart("advertisement") @Schema(description = "Данные объявления в формате JSON") AdvertisementCreateDto createDto,
                                                                      @RequestPart(value = "images", required = false) @Schema(description = "Список файлов изображений (опционально)") List<MultipartFile> images,
                                                                      Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Received request to create advertisement from user: {}", userEmail);
        AdvertisementDetailDto createdAdDto = advertisementService.createAdvertisement(createDto, images, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAdDto);
    }


    @Operation(summary = "Получить объявление по ID",
            description = "Возвращает детальную информацию об одном объявлении. Доступно всем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Объявление найдено",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdvertisementDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementDetailDto> getAdvertisement(@Parameter(description = "ID объявления", required = true, example = "1") @PathVariable Long id) {
        log.debug("Received request to get advertisement by ID: {}", id);
        AdvertisementDetailDto adDto = advertisementService.getAdvertisementById(id);
        return ResponseEntity.status(HttpStatus.OK).body(adDto);
    }

    @Operation(summary = "Получить все активные объявления (с пагинацией)",
            description = "Возвращает страницу со списком активных объявлений. Доступно всем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список объявлений получен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class))) // Указываем Page, springdoc подставит AdvertisementResponseDto
    })
    @GetMapping
    public ResponseEntity<Page<AdvertisementResponseDto>> getAllAdvertisements(
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Received request to get all advertisements, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<AdvertisementResponseDto> advertisementPage = advertisementService.getAllActiveAdvertisements(pageable);
        return ResponseEntity.ok(advertisementPage);
    }

    @Operation(summary = "Поиск и фильтрация объявлений",
            description = "Возвращает страницу с объявлениями, соответствующими критериям поиска и фильтрации. Доступно всем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список отфильтрованных объявлений получен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<AdvertisementResponseDto>> searchAdvertisements(
            @Parameter(hidden = true) // DTO для GET-параметров лучше описать полями
            @ModelAttribute AdvertisementSearchCriteriaDto criteria,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Received search request with criteria: {}, pageable: {}", criteria, pageable);
        Page<AdvertisementResponseDto> advertisementPage = advertisementService.searchAdvertisements(criteria, pageable);
        return ResponseEntity.ok(advertisementPage);
    }

    @Operation(summary = "Обновить существующее объявление",
            description = "Обновляет данные объявления, включая опциональную замену изображений. Требуется аутентификация и права владельца.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Объявление успешно обновлено",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdvertisementDetailDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (не владелец)", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Объявление, категория или город не найдены", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdvertisementDetailDto> updateAdvertisement(
            @Parameter(description = "ID обновляемого объявления", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestPart("advertisement") @Schema(description = "Данные для обновления объявления в формате JSON") AdvertisementUpdateDto updateDto,
            @RequestPart(value = "images", required = false) @Schema(description = "Новый список файлов изображений (если переданы, старые будут заменены). " +
                    "Если параметр 'images' не передан (null), изображения не изменяются. " +
                    "Если передан пустой список 'images', все текущие изображения будут удалены.") List<MultipartFile> images,
            Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Received request to update advertisement ID: {} from user: {}", id, userEmail);
        AdvertisementDetailDto adDto = advertisementService.updateAdvertisement(id, updateDto, images, userEmail);
        return ResponseEntity.status(HttpStatus.OK).body(adDto);
    }

    @Operation(summary = "Удалить объявление",
            description = "Удаляет объявление и связанные с ним изображения. Требуется аутентификация и права владельца.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Объявление успешно удалено"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (не владелец)", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvertisement(
            @Parameter(description = "ID удаляемого объявления", required = true, example = "1") @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        log.info("Received request to delete advertisement ID: {} from user: {}", id, userEmail);
        advertisementService.deleteAdvertisement(id, userEmail);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}
