package com.tech.listify.controller;

import com.tech.listify.dto.locationdto.CityDto;
import com.tech.listify.dto.locationdto.DistrictDto;
import com.tech.listify.dto.locationdto.RegionDto;
import com.tech.listify.repository.CityRepository;
import com.tech.listify.repository.DistrictRepository;
import com.tech.listify.repository.RegionRepository;
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

    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;

    @GetMapping("/regions")
    @Operation(summary = "Получить список всех областей")
    public ResponseEntity<List<RegionDto>> getAllRegions() {
        List<RegionDto> regions = regionRepository.findAll().stream()
                .map(region -> new RegionDto(region.getId(), region.getName()))
                .toList();
        return ResponseEntity.ok(regions);
    }

    @GetMapping("/districts")
    @Operation(summary = "Получить список районов для конкретной области")
    public ResponseEntity<List<DistrictDto>> getDistrictsByRegion(
            @Parameter(description = "ID области", required = true) @RequestParam Integer regionId) {
        List<DistrictDto> districts = districtRepository.findByRegionId(regionId).stream()
                .map(district -> new DistrictDto(district.getId(), district.getName()))
                .toList();
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/cities")
    @Operation(summary = "Получить список городов для конкретного района")
    public ResponseEntity<List<CityDto>> getCitiesByDistrict(
            @Parameter(description = "ID района", required = true) @RequestParam Integer districtId) {
        List<CityDto> cities = cityRepository.findByDistrictId(districtId).stream()
                .map(city -> new CityDto(city.getId(), city.getName()))
                .toList();
        return ResponseEntity.ok(cities);
    }
}