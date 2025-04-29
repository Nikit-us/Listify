package com.tech.listify.repository;

import com.tech.listify.model.AdvertisementImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvertisementImageRepository extends JpaRepository<AdvertisementImage, Long> {
    List<AdvertisementImage> findByAdvertisementId(Long advertisementId);
    Optional<AdvertisementImage> findByAdvertisementIdAndIsPreviewTrue(Long advertisementId);
}
