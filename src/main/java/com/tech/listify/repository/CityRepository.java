package com.tech.listify.repository;

import com.tech.listify.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
    List<City> findByNameInIgnoreCase(List<String> names);

    List<City> findByDistrictId(Integer districtId);
}
