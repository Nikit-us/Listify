package com.tech.listify.mapper;

import com.tech.listify.dto.cityDto.CityDto;
import com.tech.listify.model.City;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CityMapper {
    CityDto toCityResponse(City city);
}
