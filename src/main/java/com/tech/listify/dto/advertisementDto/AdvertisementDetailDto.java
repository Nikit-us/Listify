package com.tech.listify.dto.advertisementDto;

import com.tech.listify.model.enums.AdvertisementCondition;
import com.tech.listify.model.enums.AdvertisementStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AdvertisementDetailDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private AdvertisementStatus status;
    private AdvertisementCondition condition;

    private Integer categoryId;
    private String categoryName;

    private Integer cityId;
    private String cityName;

    private Long sellerId;
    private String sellerName;

    private List<AdvertisementImageDto> images;
}
