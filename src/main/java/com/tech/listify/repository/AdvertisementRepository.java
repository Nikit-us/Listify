package com.tech.listify.repository;

import com.tech.listify.model.Advertisement;
import com.tech.listify.model.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long>, JpaSpecificationExecutor<Advertisement> {
    Page<Advertisement> findBySellerId(Long sellerId, Pageable pageable);
    Page<Advertisement> findByCategoryIdAndStatus(Integer categoryId, ProductStatus status, Pageable pageable);
    Page<Advertisement> findByCityIdAndStatus(Integer cityId, ProductStatus status, Pageable pageable);
    Page<Advertisement> findByStatus(ProductStatus status, Pageable pageable);
}
