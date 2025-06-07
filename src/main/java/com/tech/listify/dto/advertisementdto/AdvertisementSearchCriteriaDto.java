package com.tech.listify.dto.advertisementdto;

import com.tech.listify.model.enums.AdvertisementCondition;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Критерии для поиска и фильтрации объявлений")
public record AdvertisementSearchCriteriaDto(
        @Schema(description = "Ключевое слово для поиска в названии и описании")
        String keyword,

        @Schema(description = "ID категории для фильтрации")
        Integer categoryId,

        @Schema(description = "ID города для фильтрации")
        Integer cityId,

        @Schema(description = "Минимальная цена")
        BigDecimal minPrice,

        @Schema(description = "Максимальная цена")
        BigDecimal maxPrice,

        @Schema(description = "Состояние товара")
        AdvertisementCondition condition,

        @Schema(description = "ID продавца для фильтрации")
        Long sellerId
) {
}