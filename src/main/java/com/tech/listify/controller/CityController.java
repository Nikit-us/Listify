package com.tech.listify.controller;

import com.tech.listify.dto.cityDto.CityDto;
import com.tech.listify.mapper.CityMapper;
import com.tech.listify.model.City;
import com.tech.listify.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
