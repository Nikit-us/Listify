package com.tech.listify.dto.locationdto.districtdto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DistrictCreateDto(
        @NotBlank(message = "Название района не может быть пустым")
        @Size(max = 100, message = "Название района не может быть длиннее 100 символов")
        @Schema(description = "Название нового района", requiredMode = Schema.RequiredMode.REQUIRED, example = "Березовский район")
        String name,
        @NotBlank(message = "id области не может быть пустым")
        @Schema(description = "id области", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        Integer regionId
        ) {
}
