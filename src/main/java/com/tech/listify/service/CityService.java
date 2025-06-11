package com.tech.listify.service;

import com.tech.listify.dto.locationdto.CityCreateDto;
import com.tech.listify.dto.locationdto.CityDto;
import com.tech.listify.model.City;

import java.util.List;

public interface CityService {
    City findCityById(Integer id);

    List<CityDto> findAllCities();

    List<CityDto> createCities(List<CityCreateDto> createDtos);
}
