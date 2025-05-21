package com.tech.listify.service;

import com.tech.listify.dto.cityDto.CityDto;
import com.tech.listify.model.City;

import java.util.List;

public interface CityService {
    City findCityById(Integer id);
    List<CityDto> findAllCities();

}
