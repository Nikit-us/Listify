package com.tech.listify.controller;

import com.tech.listify.dto.locationdto.CityDto;
import com.tech.listify.dto.locationdto.DistrictDto;
import com.tech.listify.dto.locationdto.RegionDto;
import com.tech.listify.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/regions")
    @Operation(summary = "Получить список всех областей")
    public ResponseEntity<List<RegionDto>> getAllRegions() {
        return ResponseEntity.ok(locationService.findAllRegions());
    }

    @GetMapping("/districts")
    @Operation(summary = "Получить список районов для конкретной области")
    public ResponseEntity<List<DistrictDto>> getDistrictsByRegion(
            @Parameter(description = "ID области", required = true) @RequestParam Integer regionId) {
        return ResponseEntity.ok(locationService.findAllDistricts(regionId));
    }

    @GetMapping("/cities")
    @Operation(summary = "Получить список городов для конкретного района")
    public ResponseEntity<List<CityDto>> getCitiesByDistrict(
            @Parameter(description = "ID района", required = true) @RequestParam Integer districtId) {
        return ResponseEntity.ok(locationService.findAllCities(districtId));
    }
}