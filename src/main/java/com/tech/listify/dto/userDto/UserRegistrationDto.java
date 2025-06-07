package com.tech.listify.dto.userDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для регистрации нового пользователя")
public record UserRegistrationDto(
        @NotBlank(message = "Имя не может быть пустым")
        @Size(min = 2, max = 100, message = "Имя должно содержать от 2 до 100 символов")
        @Schema(description = "Полное имя пользователя", example = "Иван Иванов", requiredMode = Schema.RequiredMode.REQUIRED)
        String fullName,

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        @Size(max = 255, message = "Email не может быть длиннее 255 символов")
        @Schema(description = "Email пользователя (используется для входа)", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 8, max = 255, message = "Пароль должен содержать от 8 до 255 символов")
        @Schema(description = "Пароль пользователя (мин. 8 символов)", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password,

        @Size(max = 50, message = "Номер телефона не может быть длиннее 50 символов")
        @Schema(description = "Номер телефона пользователя (опционально)", example = "375291234567")
        String phoneNumber,

        int cityId
) {
}