package com.tech.listify.dto.advertisementDto;

import lombok.Data;

@Data
public class AdvertisementImageDto {
    private Long id;
    private String imageUrl;
    private boolean isPreview;
}