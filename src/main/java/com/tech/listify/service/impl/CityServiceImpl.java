package com.tech.listify.service.impl;

import com.tech.listify.dto.cityDto.CityDto;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.mapper.CityMapper;
import com.tech.listify.model.City;
import com.tech.listify.repository.CityRepository;
import com.tech.listify.service.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    @Override
    @Cacheable(cacheNames = "cities", key = "#id")
    public City findCityById(Integer id) {
        log.debug("Fetching city with ID: {} from repository", id);
        return cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Город с ID " + id + " не найден."));
    }

    @Cacheable(cacheNames = "cities")
    public List<CityDto> findAllCities() {
        log.debug("Fetching all cities from repository");
        return cityRepository.findAll().stream().map(cityMapper::toCityResponse).toList();
    }
}
