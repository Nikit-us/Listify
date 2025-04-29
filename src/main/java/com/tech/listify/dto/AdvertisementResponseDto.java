package com.tech.listify.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class AdvertisementResponseDto {
    private Long id;
    private String title;
    private BigDecimal price;
    private String cityName;
    private OffsetDateTime createdAt;
    private String previewImageUrl;
}
