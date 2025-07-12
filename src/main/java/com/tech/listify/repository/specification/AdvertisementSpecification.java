package com.tech.listify.repository.specification;


import com.tech.listify.dto.advertisementdto.AdvertisementSearchCriteriaDto;
import com.tech.listify.model.*;
import com.tech.listify.model.enums.AdvertisementCondition;
import com.tech.listify.model.enums.AdvertisementStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public class AdvertisementSpecification {

    private AdvertisementSpecification() {
    }

    /**
     * Создает спецификацию на основе переданных критериев.
     *
     * @param criteria DTO с критериями поиска.
     * @return Скомбинированная спецификация.
     */
    public static Specification<Advertisement> fromCriteria(AdvertisementSearchCriteriaDto criteria) {
        Specification<Advertisement> spec = Specification.where(isActive());

        if (criteria == null) {
            return spec;
        }
        if (StringUtils.hasText(criteria.keyword())) {
            spec = spec.and(hasKeyword(criteria.keyword()));
        }
        if (criteria.categoryId() != null) {
            spec = spec.and(inCategory(criteria.categoryId()));
        }
        if (criteria.cityId() != null) {
            spec = spec.and(inCity(criteria.cityId()));
        } else if(criteria.districtId() != null) {
            spec = spec.and(inDistrict(criteria.districtId()));
        } else if(criteria.regionId() != null) {
            spec = spec.and(inRegion(criteria.regionId()));
        }
        if (criteria.minPrice() != null) {
            spec = spec.and(priceGreaterThanOrEqual(criteria.minPrice()));
        }
        if (criteria.maxPrice() != null) {
            spec = spec.and(priceLessThanOrEqual(criteria.maxPrice()));
        }
        if (criteria.condition() != null) {
            spec = spec.and(hasCondition(criteria.condition()));
        }
        if (criteria.sellerId() != null) {
            spec = spec.and(bySeller(criteria.sellerId()));
        }

        return spec;
    }


    public static Specification<Advertisement> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), AdvertisementStatus.ACTIVE);
    }

    public static Specification<Advertisement> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(keyword)) {
                return criteriaBuilder.conjunction();
            }
            String likePattern = "%" + keyword.toLowerCase() + "%";
            Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern);
            Predicate descriptionLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern);
            return criteriaBuilder.or(titleLike, descriptionLike);
        };
    }

    public static Specification<Advertisement> inCategory(Integer categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) return criteriaBuilder.conjunction();
            Join<Advertisement, Category> categoryJoin = root.join("category", JoinType.INNER);
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    public static Specification<Advertisement> inCity(Integer cityId) {
        return (root, query, criteriaBuilder) -> {
            if (cityId == null) return criteriaBuilder.conjunction();
            Join<Advertisement, City> cityJoin = root.join("city", JoinType.INNER);
            return criteriaBuilder.equal(cityJoin.get("id"), cityId);
        };
    }

    public static Specification<Advertisement> inRegion(Integer regionId) {
        return (root, query, criteriaBuilder) -> {
            if (regionId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Advertisement, City> cityJoin = root.join("city", JoinType.INNER);
            Join<City, District> districtJoin = cityJoin.join("district", JoinType.INNER);
            Join<District, Region> regionJoin = districtJoin.join("region", JoinType.INNER);
            return criteriaBuilder.equal(regionJoin.get("id"), regionId);
        };
    }

    public static Specification<Advertisement> inDistrict(Integer districtId) {
        return (root, query, criteriaBuilder) -> {
            if (districtId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Advertisement, City> cityJoin = root.join("city", JoinType.INNER);
            Join<City, District> districtJoin = cityJoin.join("district", JoinType.INNER);
            return criteriaBuilder.equal(districtJoin.get("id"), districtId);
        };
    }

    public static Specification<Advertisement> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<Advertisement> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<Advertisement> hasCondition(AdvertisementCondition condition) {
        return (root, query, criteriaBuilder) -> {
            if (condition == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("condition"), condition);
        };
    }

    public static Specification<Advertisement> bySeller(Long sellerId) {
        return (root, query, criteriaBuilder) -> {
            if (sellerId == null) return criteriaBuilder.conjunction();
            Join<Advertisement, User> sellerJoin = root.join("seller", JoinType.INNER);
            return criteriaBuilder.equal(sellerJoin.get("id"), sellerId);
        };
    }
}
