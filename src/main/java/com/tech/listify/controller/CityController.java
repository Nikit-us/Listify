package com.tech.listify.controller;

import com.tech.listify.dto.citydto.CityCreateDto;
import com.tech.listify.dto.citydto.CityDto;
import com.tech.listify.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@Slf4j
public class CityController {
    private final CityService cityService;

    @Operation(summary = "Получить список всех городов",
            description = "Возвращает полный список доступных городов. Доступно всем.")
    @ApiResponse(responseCode = "200", description = "Список городов успешно получен",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = CityDto.class))))
    @GetMapping
    public ResponseEntity<List<CityDto>> getAll() {
        List<CityDto> cities = cityService.findAllCities();
        return ResponseEntity.ok(cities);
    }

    @Operation(summary = "Создать один или несколько городов (только для ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Город успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "409", description = "Город уже существует")
    })
    @PostMapping
    public ResponseEntity<List<CityDto>> createCities(@Valid @RequestBody List<CityCreateDto> createDtos) {
        if (createDtos == null || createDtos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Список для создания не может быть пустым.");
        }
        List<CityDto> createdCities = cityService.createCities(createDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCities);
    }
}
