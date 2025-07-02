package com.tech.listify.service;

import com.tech.listify.dto.locationdto.citydto.CityCreateDto;
import com.tech.listify.dto.locationdto.citydto.CityDto;
import com.tech.listify.dto.locationdto.districtdto.DistrictDto;
import com.tech.listify.dto.locationdto.regiondto.RegionDto;
import com.tech.listify.model.City;
import com.tech.listify.model.District;
import com.tech.listify.model.Region;

import java.util.List;

public interface LocationService {
    CityDto findCityById(Integer id);

    Region findRegionById(Integer id);

    District findDistrictById(Integer id);

    List<CityDto> findAllCities(Integer districtId);

    List<DistrictDto> findAllDistricts(Integer regionId);

    List<RegionDto> findAllRegions();

    List<CityDto> createCities(List<CityCreateDto> createDto);
}
