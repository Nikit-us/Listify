package com.tech.listify.repository;

import com.tech.listify.model.Advertisement;
import com.tech.listify.model.enums.AdvertisementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long>, JpaSpecificationExecutor<Advertisement> {
    Page<Advertisement> findByStatus(AdvertisementStatus status, Pageable pageable);
    int countBySellerIdAndStatus(Long sellerId, AdvertisementStatus status);
}
