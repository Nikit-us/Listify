package com.tech.listify.dto.userDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с JWT токеном и информацией о пользователе после успешного входа")
public class JwtResponseDto {
    @Schema(description = "JWT токен доступа", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImlhdCI6MTcxNTI1NzgwMywiZXhwIjoxNzE1MjYxNDAzfQ.abcdef...")
    private String token;
    @Schema(description = "Тип токена", example = "Bearer", defaultValue = "Bearer")
    private String type = "Bearer";


    @Schema(description = "ID аутентифицированного пользователя", example = "1")
    private Long userId;
    @Schema(description = "Email аутентифицированного пользователя", example = "user@example.com")
    private String email;
    @Schema(description = "Список ролей пользователя", example = "[\"ROLE_USER\"]")
    private List<String> roles;

    public JwtResponseDto(String token, Long userId, String email, List<String> roles) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.roles = roles;
    }
}
