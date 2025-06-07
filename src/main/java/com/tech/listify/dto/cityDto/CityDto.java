package com.tech.listify.dto.cityDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о городе")
public record CityDto(
        @Schema(description = "ID города")
        Integer id,

        @Schema(description = "Название города")
        String name
) {
}