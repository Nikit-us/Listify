package com.tech.listify.mapper;

import com.tech.listify.dto.cityDto.CityCreateDto;
import com.tech.listify.dto.cityDto.CityDto;
import com.tech.listify.model.City;

public interface CityMapper {
    CityDto toCityResponse(City city);
    City toEntity(CityCreateDto cityCreateDto);
}
