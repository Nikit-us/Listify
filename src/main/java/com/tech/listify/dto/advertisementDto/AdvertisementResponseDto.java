package com.tech.listify.dto.advertisementDto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Краткая информация об объявлении для списков")
public record AdvertisementResponseDto(
        @Schema(description = "ID объявления")
        Long id,

        @Schema(description = "Заголовок")
        String title,

        @Schema(description = "Цена")
        BigDecimal price,

        @Schema(description = "ID города")
        Integer cityId,

        @Schema(description = "Название города")
        String cityName,

        @Schema(description = "Дата создания")
        OffsetDateTime createdAt,

        @Schema(description = "URL изображения для предпросмотра")
        String previewImageUrl
) {
}