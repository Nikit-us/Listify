package com.tech.listify.dto.advertisementDto;

import com.tech.listify.model.enums.AdvertisementCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Данные для создания нового объявления")
public class AdvertisementCreateDto {
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 5, max = 50,  message = "Заголовок должен содержать от 5 до 50 символов")
    @Schema(description = "Заголовок объявления", example = "Продам ноутбук", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Size(max = 5000, message = "Описание не может быть длиннее 5000 символов")
    @Schema(description = "Подробное описание объявления", example = "Отличный ноутбук, почти новый...", nullable = true)
    private String description;

    @NotNull(message = "Цена не должна быть пустой")
    @PositiveOrZero(message = "Цена должна быть положительной или нулевой")
    @Digits(integer=10, fraction=2, message = "Некорректный формат цены (максимум 10 цифр до точки, 2 после)")
    @Schema(description = "Цена объявления", example = "550.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotNull(message = "ID категории не может быть пустым")
    @Schema(description = "ID категории объявления", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer categoryId;

    @NotNull(message = "ID города не может быть пустым")
    @Schema(description = "ID города, в котором находится товар/услуга", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cityId;

    @Schema(description = "Состояние товара (NEW, USED_PERFECT, и т.д.)", example = "USED_GOOD", nullable = true)
    private AdvertisementCondition condition;
}
