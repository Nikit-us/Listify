package com.tech.listify.dto;

import com.tech.listify.model.enums.AdvertisementCondition;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdvertisementCreateDto {
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 5, max = 50,  message = "Заголовок должен содержать от 5 до 50 символов")
    private String title;

    @Size(max = 5000, message = "Описание не может быть длиннее 5000 символов")
    private String description;

    @NotNull(message = "Цена не должна быть пустой")
    @PositiveOrZero(message = "Цена должна быть положительной или нулевой")
    @Digits(integer=10, fraction=2, message = "Некорректный формат цены (максимум 10 цифр до точки, 2 после)")
    private BigDecimal price;

    @NotNull(message = "ID категории не может быть пустым")
    private Integer categoryId;

    @NotNull(message = "ID города не может быть пустым")
    private Integer cityId;

    private AdvertisementCondition condition;

    private List<String> imageUrls;
}
