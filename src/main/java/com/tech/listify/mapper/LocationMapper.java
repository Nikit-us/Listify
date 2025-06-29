package com.tech.listify.mapper;

import com.tech.listify.dto.locationdto.CityCreateDto;
import com.tech.listify.dto.locationdto.CityDto;
import com.tech.listify.model.City;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    CityDto toCityResponse(City city);

    List<CityDto> toCityResponseList(List<City> cities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "advertisements", ignore = true)
    @Mapping(target = "district", ignore = true)
    City toEntity(CityCreateDto dto);

    List<City> toEntityList(List<CityCreateDto> dtoList);
}