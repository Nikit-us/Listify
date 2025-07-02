package com.tech.listify.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiError", description = "Стандартный формат ответа об ошибке")
public record ApiErrorResponse(
        @Schema(description = "Время и дата возникновения ошибки в формате ISO-8601", example = "2025-07-02T22:15:30.123Z")
        String timestamp,

        @Schema(description = "HTTP статус код", example = "404")
        int status,

        @Schema(description = "Краткий тип ошибки", example = "Not Found")
        String error,

        @Schema(description = "Детальное сообщение об ошибке. Может быть строкой или объектом (например, для ошибок валидации).",
                example = "Ресурс с ID 123 не найден.")
        Object message,

        @Schema(description = "Путь запроса, на котором произошла ошибка", example = "/api/ads/123")
        String path
) {
}