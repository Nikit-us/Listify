package com.tech.listify.service.impl;

import com.tech.listify.dto.locationdto.CityCreateDto;
import com.tech.listify.dto.locationdto.CityDto;
import com.tech.listify.exception.ResourceAlreadyExistsException;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.mapper.CityMapper;
import com.tech.listify.model.City;
import com.tech.listify.repository.CityRepository;
import com.tech.listify.service.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    @Transactional
    @CacheEvict(cacheNames = "cities", allEntries = true)
    public List<CityDto> createCities(List<CityCreateDto> createDtos) {
        log.info("Attempting to create {} new cities.", createDtos.size());

        List<String> namesToCreate = createDtos.stream()
                .map(dto -> dto.name().toLowerCase())
                .toList();

        List<City> existingCities = cityRepository.findByNameInIgnoreCase(namesToCreate);
        if (!existingCities.isEmpty()) {
            String existingNames = existingCities.stream()
                    .map(City::getName)
                    .collect(Collectors.joining(", "));
            throw new ResourceAlreadyExistsException("Города уже существуют: " + existingNames);
        }

        List<City> newCities = cityMapper.toEntityList(createDtos);

        List<City> savedCities = cityRepository.saveAll(newCities);

        log.info("Successfully created {} cities.", savedCities.size());
        return cityMapper.toCityResponseList(savedCities);
    }
}
