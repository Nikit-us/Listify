package com.tech.listify.dto.locationdto.districtdto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DistrictDto(
        @Schema(description = "ID района")
        Integer id,
        @Schema(description = "Название района")
        String name,
        @Schema(description = "ID области")
        Integer regionId) {
}