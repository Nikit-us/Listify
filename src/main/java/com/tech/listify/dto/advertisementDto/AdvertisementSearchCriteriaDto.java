package com.tech.listify.dto.advertisementDto;

import com.tech.listify.model.enums.AdvertisementCondition;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdvertisementSearchCriteriaDto {
    private String keyword;
    private Integer categoryId;
    private Integer cityId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private AdvertisementCondition condition;
    private Long sellerId;
}
