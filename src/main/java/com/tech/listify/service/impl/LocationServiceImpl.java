package com.tech.listify.service.impl;

import com.tech.listify.dto.locationdto.citydto.CityCreateDto;
import com.tech.listify.dto.locationdto.citydto.CityDto;
import com.tech.listify.dto.locationdto.districtdto.DistrictDto;
import com.tech.listify.dto.locationdto.regiondto.RegionDto;
import com.tech.listify.exception.ResourceNotFoundException;
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
    public CityDto findCityById(Integer id) {
        log.debug("Fetching city with ID: {}", id);
        City city = cityRepository.findById(id).orElseThrow( () -> new ResourceNotFoundException("Город с id " + id + " не найден"));
        return locationMapper.toCityDto(city);
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
        return cityRepository.findByDistrictId(districtId)
                .stream().map(locationMapper::toCityDto).toList();
    }

    @Override
    @Cacheable(value = "districts", key = "#regionId")
    public List<DistrictDto> findAllDistricts(Integer regionId) {
        return districtRepository.findByRegionId(regionId)
                .stream().map(locationMapper::toDistrictDto).toList();
    }

    @Override
    @Cacheable(value = "regions", key = "0")
    public List<RegionDto> findAllRegions() {
        return regionRepository.findAll()
                .stream().map(locationMapper::toRegionDto).toList();
    }

    @Override
    public List<CityDto> createCities(List<CityCreateDto> createDto) {

        return List.of();
    }
}
