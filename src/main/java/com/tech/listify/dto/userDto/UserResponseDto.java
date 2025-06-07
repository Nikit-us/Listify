package com.tech.listify.dto.userDto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Данные пользователя, возвращаемые после успешной регистрации или получения профиля")
public record UserResponseDto(
        @Schema(description = "Уникальный идентификатор пользователя", example = "1")
        Long id,

        @Schema(description = "Email пользователя", example = "user@example.com")
        String email,

        @Schema(description = "Полное имя пользователя", example = "Иван Иванов")
        String fullName,

        @Schema(description = "Номер телефона пользователя", example = "+375291234567", nullable = true)
        String phoneNumber,

        @Schema(description = "Дата и время регистрации пользователя")
        OffsetDateTime registeredAt,

        @Schema(description = "URL аватара пользователя", example = "/uploads/avatars/uuid.jpg", nullable = true)
        String avatarUrl
) {
}