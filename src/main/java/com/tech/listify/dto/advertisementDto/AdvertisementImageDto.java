package com.tech.listify.dto.advertisementDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация об изображении объявления")
public record AdvertisementImageDto(
        @Schema(description = "ID изображения")
        Long id,

        @Schema(description = "URL изображения")
        String imageUrl,

        @Schema(description = "Является ли изображение превью")
        boolean isPreview
) {
}