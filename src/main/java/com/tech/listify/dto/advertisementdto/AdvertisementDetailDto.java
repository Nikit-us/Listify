package com.tech.listify.dto.advertisementdto;

import com.tech.listify.model.enums.AdvertisementCondition;
import com.tech.listify.model.enums.AdvertisementStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Полная информация об объявлении")
public record AdvertisementDetailDto(
        @Schema(description = "ID объявления")
        Long id,

        @Schema(description = "Заголовок")
        String title,

        @Schema(description = "Описание")
        String description,

        @Schema(description = "Цена")
        BigDecimal price,

        @Schema(description = "Дата создания")
        OffsetDateTime createdAt,

        @Schema(description = "Дата последнего обновления")
        OffsetDateTime updatedAt,

        @Schema(description = "Статус (активно, продано и т.д.)")
        AdvertisementStatus status,

        @Schema(description = "Состояние (новое, б/у)")
        AdvertisementCondition condition,

        @Schema(description = "ID категории")
        Integer categoryId,

        @Schema(description = "Название категории")
        String categoryName,

        @Schema(description = "ID города")
        Integer cityId,

        @Schema(description = "Название города")
        String cityName,

        @Schema(description = "ID продавца")
        Long sellerId,

        @Schema(description = "Имя продавца")
        String sellerName,

        @Schema(description = "Список изображений объявления")
        List<AdvertisementImageDto> images
) {
}