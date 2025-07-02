package com.tech.listify.dto.locationdto.regiondto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegionDto(
        @Schema(description = "ID области")
        Integer id,
        @Schema(description = "Название области")
        String name) {

}