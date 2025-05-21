package com.tech.listify.dto.userDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Schema(description = "Данные пользователя, возвращаемые после успешной регистрации или получения профиля")
public class UserResponseDto {
    @Schema(description = "Уникальный идентификатор пользователя", example = "1")
    private Long id;
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;
    @Schema(description = "Полное имя пользователя", example = "Иван Иванов")
    private String fullName;
    @Schema(description = "Номер телефона пользователя", example = "+375291234567", nullable = true)
    private String phoneNumber;
    @Schema(description = "Дата и время регистрации пользователя")
    private OffsetDateTime registeredAt;
    @Schema(description = "URL аватара пользователя", example = "/uploads/avatars/uuid.jpg", nullable = true)
    private String avatarUrl;
}
