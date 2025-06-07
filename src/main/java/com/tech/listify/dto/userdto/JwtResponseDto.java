package com.tech.listify.dto.userdto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Ответ с JWT токеном и информацией о пользователе после успешного входа")
public record JwtResponseDto(
        @Schema(description = "JWT токен доступа", example = "eyJhbGciOiJIUzUxMiJ9...")
        String token,

        @Schema(description = "Тип токена", example = "Bearer")
        String type,

        @Schema(description = "ID аутентифицированного пользователя", example = "1")
        Long userId,

        @Schema(description = "Email аутентифицированного пользователя", example = "user@example.com")
        String email,

        @Schema(description = "Список ролей пользователя", example = "[\"ROLE_USER\"]")
        List<String> roles
) {
}