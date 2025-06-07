package com.tech.listify.repository;

import com.tech.listify.model.AdvertisementImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementImageRepository extends JpaRepository<AdvertisementImage, Long> {
}
