package com.tech.listify.dto.advertisementDto;

import com.tech.listify.model.enums.AdvertisementCondition;
import com.tech.listify.model.enums.AdvertisementStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AdvertisementUpdateDto(
        @Size(min = 5, max = 255, message = "Заголовок должен содержать от 5 до 255 символов")
        String title,

        @Size(max = 5000, message = "Описание не может быть длиннее 5000 символов")
        String description,

        @PositiveOrZero(message = "Цена должна быть положительной или нулевой")
        @Digits(integer=10, fraction=2, message = "Некорректный формат цены (максимум 10 цифр до точки, 2 после)")
        BigDecimal price,

        Integer categoryId,
        Integer cityId,
        AdvertisementCondition condition,
        AdvertisementStatus status
) {
}