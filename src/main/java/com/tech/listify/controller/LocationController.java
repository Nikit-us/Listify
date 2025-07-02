package com.tech.listify.controller;

import com.tech.listify.dto.ApiErrorResponse;
import com.tech.listify.dto.locationdto.citydto.CityDto;
import com.tech.listify.dto.locationdto.districtdto.DistrictDto;
import com.tech.listify.dto.locationdto.regiondto.RegionDto;
import com.tech.listify.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "API для получения информации о регионах, районах и городах")
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "Получить список всех областей",
            description = "Возвращает полный список областей (регионов). Доступно всем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список областей успешно получен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = RegionDto.class))))
    })
    @GetMapping("/regions")
    public ResponseEntity<List<RegionDto>> getAllRegions() {
        return ResponseEntity.ok(locationService.findAllRegions());
    }

    @Operation(summary = "Получить список районов для конкретной области",
            description = "Возвращает все районы, принадлежащие области с указанным ID. Доступно всем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список районов успешно получен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = DistrictDto.class)))),
            @ApiResponse(responseCode = "404", description = "Область с указанным ID не найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/districts")
    public ResponseEntity<List<DistrictDto>> getDistrictsByRegion(
            @Parameter(description = "ID области (региона), для которой нужно получить районы", required = true, example = "1")
            @RequestParam Integer regionId) {
        return ResponseEntity.ok(locationService.findAllDistricts(regionId));
    }

    @Operation(summary = "Получить список городов для конкретного района",
            description = "Возвращает все города, принадлежащие району с указанным ID. Доступно всем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список городов успешно получен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CityDto.class)))),
            @ApiResponse(responseCode = "404", description = "Район с указанным ID не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/cities")
    public ResponseEntity<List<CityDto>> getCitiesByDistrict(
            @Parameter(description = "ID района, для которого нужно получить города", required = true, example = "1")
            @RequestParam Integer districtId) {
        return ResponseEntity.ok(locationService.findAllCities(districtId));
    }
}