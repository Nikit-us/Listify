package com.tech.listify.dto.userdto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Публичный профиль пользователя")
public record UserProfileDto(
        @Schema(description = "ID пользователя", example = "1")
        Long id,

        @Schema(description = "Полное имя пользователя", example = "Иван Петров")
        String fullName,

        @Schema(description = "email пользователя")
        String email,

        @Schema(description = "Id города пользователя", example = "1", nullable = true)
        Integer cityId,

        @Schema(description = "Название города пользователя", example = "Минск", nullable = true)
        String cityName,

        @Schema(description = "Номер телефона")
        String phoneNumber,

        @Schema(description = "Дата и время регистрации")
        OffsetDateTime registeredAt,

        @Schema(description = "Общее количество активных объявлений пользователя", example = "5")
        Integer totalActiveAdvertisements,

        @Schema(description = "URL аватара пользователя", example = "/uploads/avatars/avatar_uuid.jpg", nullable = true)
        String avatarUrl
) {
}