package com.tech.listify.service.impl;

import com.tech.listify.dto.locationdto.CityCreateDto;
import com.tech.listify.dto.locationdto.CityDto;
import com.tech.listify.dto.locationdto.DistrictDto;
import com.tech.listify.dto.locationdto.RegionDto;
import com.tech.listify.mapper.LocationMapper;
import com.tech.listify.model.City;
import com.tech.listify.model.District;
import com.tech.listify.model.Region;
import com.tech.listify.repository.DistrictRepository;
import com.tech.listify.repository.CityRepository;
import com.tech.listify.repository.RegionRepository;
import com.tech.listify.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;
    private final LocationMapper locationMapper;


    @Override
    public City findCityById(Integer id) {
        return null;
    }

    @Override
    public Region findRegionById(Integer id) {
        return null;
    }

    @Override
    public District findDistrictById(Integer id) {
        return null;
    }

    @Override
    @Cacheable(value = "cities", key = "#districtId")
    public List<CityDto> findAllCities(Integer districtId) {
        return cityRepository.findByDistrictId(districtId).stream()
                .map(city -> new CityDto(city.getId(), city.getName()))
                .toList();
    }

    @Override
    @Cacheable(value = "districts", key = "#regionId")
    public List<DistrictDto> findAllDistricts(Integer regionId) {
        return districtRepository.findByRegionId(regionId).stream()
                .map(district -> new DistrictDto(district.getId(), district.getName()))
                .toList();
    }

    @Override
    @Cacheable(value = "regions", key = "0")
    public List<RegionDto> findAllRegions() {
        return regionRepository.findAll().stream()
                .map(region -> new RegionDto(region.getId(), region.getName()))
                .toList();
    }

    @Override
    public List<CityDto> createCities(List<CityCreateDto> createDtos) {
        return List.of();
    }
}
