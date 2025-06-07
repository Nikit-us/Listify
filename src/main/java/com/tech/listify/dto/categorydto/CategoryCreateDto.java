package com.tech.listify.dto.categorydto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для создания новой категории")
public record CategoryCreateDto(
        @NotBlank(message = "Название категории не может быть пустым")
        @Size(max = 100, message = "Название категории не может быть длиннее 100 символов")
        @Schema(description = "Название новой категории", requiredMode = Schema.RequiredMode.REQUIRED, example = "Хобби и отдых")
        String name,

        @Schema(description = "ID родительской категории (опционально, для создания подкатегории)", example = "1")
        Integer parentCategoryId
) {
}