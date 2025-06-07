package com.tech.listify.dto.citydto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для создания нового города")
public record CityCreateDto(
        @NotBlank(message = "Название города не может быть пустым")
        @Size(max = 100, message = "Название города не может быть длиннее 100 символов")
        @Schema(description = "Название нового города", requiredMode = Schema.RequiredMode.REQUIRED, example = "Новосибирск")
        String name
) {
}