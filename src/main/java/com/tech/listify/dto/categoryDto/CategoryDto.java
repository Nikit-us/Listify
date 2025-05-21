package com.tech.listify.dto.categoryDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Информация о категории объявлений")
public class CategoryDto {
    @Schema(description = "Уникальный идентификатор категории", example = "1")
    private Integer id;

    @Schema(description = "Название категории", example = "Электроника")
    private String name;
}
