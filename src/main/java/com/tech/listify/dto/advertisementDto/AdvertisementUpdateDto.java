package com.tech.listify.dto.advertisementDto;

import com.tech.listify.model.enums.AdvertisementCondition;
import com.tech.listify.model.enums.AdvertisementStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdvertisementUpdateDto {

    @Size(min = 5, max = 255, message = "Заголовок должен содержать от 5 до 255 символов")
    private String title;

    @Size(max = 5000, message = "Описание не может быть длиннее 5000 символов")
    private String description;

    @PositiveOrZero(message = "Цена должна быть положительной или нулевой")
    @Digits(integer=10, fraction=2, message = "Некорректный формат цены (максимум 10 цифр до точки, 2 после)")
    private BigDecimal price;

    private Integer categoryId;

    private Integer cityId;

    private AdvertisementCondition condition;

    private AdvertisementStatus status;
}
