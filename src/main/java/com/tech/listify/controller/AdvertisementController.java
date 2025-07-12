package com.tech.listify.controller;

import com.tech.listify.dto.ApiErrorResponse;
import com.tech.listify.dto.PageResponseDto;
import com.tech.listify.dto.advertisementdto.*;
import com.tech.listify.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Advertisements", description = "API для управления объявлениями")
public class AdvertisementController {
    private final AdvertisementService advertisementService;

    @Operation(summary = "Создать новое объявление",
            description = "Создает новое объявление с текстовыми данными и опциональными изображениями. " +
                    "Запрос должен быть в формате `multipart/form-data`. " +
                    "Требуется аутентификация пользователя.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Объявление успешно создано",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdvertisementDetailDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или некорректный запрос",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Связанный ресурс (категория/город) не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера (например, ошибка сохранения файла)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdvertisementDetailDto> createAdvertisement(
            @Parameter(description = "Данные объявления в формате JSON", required = true)
            @Valid @RequestPart("advertisement") AdvertisementCreateDto createDto,
            @Parameter(description = "Список файлов изображений (опционально, до 10MB на файл)")
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
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
            @ApiResponse(responseCode = "404", description = "Объявление не найдено",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementDetailDto> getAdvertisement(
            @Parameter(description = "ID объявления", required = true, example = "1")
            @PathVariable Long id) {
        log.debug("Received request to get advertisement by ID: {}", id);
        AdvertisementDetailDto adDto = advertisementService.getAdvertisementById(id);
        return ResponseEntity.status(HttpStatus.OK).body(adDto);
    }

    @Operation(summary = "Получить все активные объявления (с пагинацией)",
            description = "Возвращает страницу со списком активных объявлений. Доступно всем.")
    @Parameters({
            @Parameter(name = "page", in = ParameterIn.QUERY, description = "Номер страницы (начиная с 0)", example = "0"),
            @Parameter(name = "size", in = ParameterIn.QUERY, description = "Количество элементов на странице", example = "20"),
            @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Сортировка. Формат: `property,direction`. Например: `price,asc` или `createdAt,desc`", example = "createdAt,desc")
    })
    @ApiResponse(responseCode = "200", description = "Список объявлений получен",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PageResponseDto.class)))
    @GetMapping
    public ResponseEntity<PageResponseDto<AdvertisementResponseDto>> getAllAdvertisements(
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Received request to get all advertisements, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        PageResponseDto<AdvertisementResponseDto> advertisementPage = advertisementService.getAllActiveAdvertisements(pageable);
        return ResponseEntity.ok(advertisementPage);
    }

    @Operation(summary = "Поиск и фильтрация объявлений",
            description = "Возвращает страницу с объявлениями, соответствующими критериям поиска и фильтрации. Доступно всем.")
    @Parameters({
            @Parameter(name = "keyword", description = "Ключевое слово для поиска в названии и описании", example = "ноутбук"),
            @Parameter(name = "categoryId", description = "ID категории для фильтрации", example = "3"),
            @Parameter(name = "regionId", description = "ID области для фильтрации", example = "1"),
            @Parameter(name = "districtId", description = "ID района для фильтрации", example = "1"),
            @Parameter(name = "cityId", description = "ID города для фильтрации", example = "1"),
            @Parameter(name = "minPrice", description = "Минимальная цена", example = "500"),
            @Parameter(name = "maxPrice", description = "Максимальная цена", example = "1500"),
            @Parameter(name = "condition", description = "Состояние товара (NEW, USED_GOOD и т.д.)", schema = @Schema(implementation = com.tech.listify.model.enums.AdvertisementCondition.class)),
            @Parameter(name = "sellerId", description = "ID продавца для фильтрации", example = "2"),
            @Parameter(name = "page", in = ParameterIn.QUERY, description = "Номер страницы (начиная с 0)", example = "0"),
            @Parameter(name = "size", in = ParameterIn.QUERY, description = "Количество элементов на странице", example = "20"),
            @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Сортировка. Формат: `property,direction`.", example = "price,asc")
    })
    @ApiResponse(responseCode = "200", description = "Список отфильтрованных объявлений получен",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PageResponseDto.class)))
    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<AdvertisementResponseDto>> searchAdvertisements(
            @Parameter(hidden = true)
            @ModelAttribute AdvertisementSearchCriteriaDto criteria,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Received search request with criteria: {}, pageable: {}", criteria, pageable);
        PageResponseDto<AdvertisementResponseDto> advertisementPage = advertisementService.searchAdvertisements(criteria, pageable);
        return ResponseEntity.ok(advertisementPage);
    }

    @Operation(summary = "Обновить существующее объявление",
            description = "Обновляет данные объявления. Позволяет выборочно удалять старые изображения и загружать новые. " +
                    "Запрос должен быть в формате `multipart/form-data`. " +
                    "Для удаления изображений передайте их ID в поле `imageIdsToDelete` внутри JSON-части 'advertisement'. " +
                    "Для добавления новых изображений передайте их как файлы в multipart-части 'images'.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Объявление успешно обновлено",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdvertisementDetailDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (не владелец)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Объявление, категория или город не найдены", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdvertisementDetailDto> updateAdvertisement(
            @Parameter(description = "ID обновляемого объявления", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Данные для обновления в формате JSON, включая опциональный список ID изображений для удаления.")
            @Valid @RequestPart("advertisement") AdvertisementUpdateDto updateDto,
            @Parameter(description = "Список НОВЫХ файлов изображений для добавления к объявлению.")
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Received request to update advertisement ID: {} from user: {}", id, userEmail);
        AdvertisementDetailDto adDto = advertisementService.updateAdvertisement(id, updateDto, images, userEmail);
        return ResponseEntity.status(HttpStatus.OK).body(adDto);
    }

    @Operation(summary = "Удалить объявление",
            description = "Удаляет объявление и связанные с ним изображения. Требуется аутентификация и права владельца объявления.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Объявление успешно удалено"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (не владелец)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvertisement(
            @Parameter(description = "ID удаляемого объявления", required = true, example = "1")
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        log.info("Received request to delete advertisement ID: {} from user: {}", id, userEmail);
        advertisementService.deleteAdvertisement(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}