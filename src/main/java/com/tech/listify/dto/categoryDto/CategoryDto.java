package com.tech.listify.dto.categoryDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о категории объявлений")
public record CategoryDto(
        @Schema(description = "Уникальный идентификатор категории", example = "1")
        Integer id,

        @Schema(description = "Название категории", example = "Электроника")
        String name
) {
}