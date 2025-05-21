package com.tech.listify.dto.userDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Schema(description = "Публичный профиль пользователя")
public class UserProfileDto {
    @Schema(description = "ID пользователя", example = "1")
    private Long id;
    @Schema(description = "Полное имя пользователя", example = "Иван Петров")
    private String fullName;
    @Schema(description = "email пользователя")
    private String email;
    @Schema(description = "Id города пользователя", example = "1", nullable = true)
    private Integer cityId;
    @Schema(description = "Номер телефона")
    private String phoneNumber;
    @Schema(description = "Дата и время регистрации")
    private OffsetDateTime registeredAt;
    @Schema(description = "Общее количество активных объявлений пользователя", example = "5")
    private Integer totalActiveAdvertisements;
    @Schema(description = "URL аватара пользователя", example = "/uploads/avatars/avatar_uuid.jpg", nullable = true)
    private String avatarUrl;
}
