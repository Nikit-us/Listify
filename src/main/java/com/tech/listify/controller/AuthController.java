package com.tech.listify.controller;

import com.tech.listify.dto.userDto.JwtResponseDto;
import com.tech.listify.dto.userDto.LoginRequestDto;
import com.tech.listify.dto.userDto.UserRegistrationDto;
import com.tech.listify.dto.userDto.UserResponseDto;
import com.tech.listify.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API для регистрации и входа пользователей")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Регистрация нового пользователя",
            description = "Создает новый аккаунт пользователя с опциональным аватаром. " +
                    "Данные пользователя передаются как JSON часть 'user', аватар (если есть) как файловая часть 'avatar'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных или некорректный запрос",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера (например, при сохранении файла)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestPart("user") @Schema(description = "Данные для регистрации пользователя в формате JSON") UserRegistrationDto registrationDto,
                                                    @RequestPart(value = "avatar", required = false) @Schema(description = "Файл аватара пользователя (опционально)") MultipartFile avatarFile) throws IOException {
        log.info("Received registration request for email: {}", registrationDto.email());
        UserResponseDto responseDto = authService.register(registrationDto, avatarFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Аутентификация пользователя",
            description = "Проверяет учетные данные пользователя и в случае успеха возвращает JWT токен.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = JwtResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Ошибка аутентификации (неверный email или пароль)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody @Schema(description = "Учетные данные для входа в систему") LoginRequestDto loginRequestDto) {
        log.info("Received login request for email: {}", loginRequestDto.email());
        JwtResponseDto jwtResponseDto = authService.login(loginRequestDto);
        log.info("Login successful for email: {}", loginRequestDto.email());
        return ResponseEntity.ok(jwtResponseDto);
    }
}
