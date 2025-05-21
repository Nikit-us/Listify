package com.tech.listify.mapper.impl;

import com.tech.listify.dto.cityDto.CityCreateDto;
import com.tech.listify.dto.cityDto.CityDto;
import com.tech.listify.mapper.CityMapper;
import com.tech.listify.model.City;
import org.springframework.stereotype.Component;

@Component
public class CityMapperImpl implements CityMapper {
    @Override
    public CityDto toCityResponse(City city) {
        if(city == null) {
            return null;
        }

        CityDto cityDto = new CityDto();
        cityDto.setId(city.getId());
        cityDto.setName(city.getName());
        return cityDto;
    }

    @Override
    public City toEntity(CityCreateDto cityCreateDto) {
        return null;
    }
}
