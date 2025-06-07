package com.tech.listify.service;

import com.tech.listify.dto.citydto.CityCreateDto;
import com.tech.listify.dto.citydto.CityDto;
import com.tech.listify.model.City;

import java.util.List;

public interface CityService {
    City findCityById(Integer id);

    List<CityDto> findAllCities();

    List<CityDto> createCities(List<CityCreateDto> createDtos);
}
