package com.tech.listify.mapper;

import com.tech.listify.dto.locationdto.citydto.CityCreateDto;
import com.tech.listify.dto.locationdto.citydto.CityDto;
import com.tech.listify.dto.locationdto.districtdto.DistrictCreateDto;
import com.tech.listify.dto.locationdto.districtdto.DistrictDto;
import com.tech.listify.dto.locationdto.regiondto.RegionDto;
import com.tech.listify.model.City;
import com.tech.listify.model.District;
import com.tech.listify.model.Region;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    @Mapping(target = "id", ignore = true)
    City toCity(CityCreateDto dto);

    @Mapping(target = "districtId",source = "city.district.id")
    CityDto toCityDto(City city);

    @Mapping(target = "id", ignore = true)
    District toDistrict(DistrictCreateDto dto);

    @Mapping(target = "regionId", source = "district.region.id")
    DistrictDto toDistrictDto(District district);

    @Mapping(target = "id", ignore = true)
    Region toRegion(RegionDto dto);

    RegionDto toRegionDto(Region region);
}