package com.tech.listify.service;

import com.tech.listify.dto.locationdto.CityCreateDto;
import com.tech.listify.dto.locationdto.CityDto;
import com.tech.listify.dto.locationdto.DistrictDto;
import com.tech.listify.dto.locationdto.RegionDto;
import com.tech.listify.model.City;
import com.tech.listify.model.District;
import com.tech.listify.model.Region;

import java.util.List;

public interface LocationService {
    City findCityById(Integer id);

    Region findRegionById(Integer id);

    District findDistrictById(Integer id);

    List<CityDto> findAllCities(Integer districtId);

    List<DistrictDto> findAllDistricts(Integer regionId);

    List<RegionDto> findAllRegions();

    List<CityDto> createCities(List<CityCreateDto> createDtos);
}
