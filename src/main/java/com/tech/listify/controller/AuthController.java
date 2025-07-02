package com.tech.listify.controller;

import com.tech.listify.dto.ApiErrorResponse;
import com.tech.listify.dto.userdto.JwtResponseDto;
import com.tech.listify.dto.userdto.LoginRequestDto;
import com.tech.listify.dto.userdto.UserRegistrationDto;
import com.tech.listify.dto.userdto.UserResponseDto;
import com.tech.listify.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API для регистрации и входа пользователей")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Регистрация нового пользователя",
            description = "Создает новый аккаунт пользователя с опциональным аватаром. " +
                    "Запрос должен быть в формате `multipart/form-data`, содержащий две части: " +
                    "1. `user`: JSON-объект с данными пользователя (`UserRegistrationDto`). " +
                    "2. `avatar`: Файл изображения (опционально).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера (например, при сохранении файла)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> register(
            @Parameter(description = "Данные для регистрации пользователя в формате JSON.", required = true)
            @Valid @RequestPart("user") UserRegistrationDto registrationDto,
            @Parameter(description = "Файл аватара пользователя (опционально).")
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) throws IOException {
        log.info("Received registration request for email: {}", registrationDto.email());
        UserResponseDto responseDto = authService.register(registrationDto, avatarFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Аутентификация пользователя",
            description = "Проверяет учетные данные пользователя и в случае успеха возвращает JWT токен вместе с основной информацией о пользователе.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = JwtResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Ошибка аутентификации (неверный email или пароль)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Учетные данные для входа в систему.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequestDto.class))
            )
            @Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("Received login request for email: {}", loginRequestDto.email());
        JwtResponseDto jwtResponseDto = authService.login(loginRequestDto);
        log.info("Login successful for email: {}", loginRequestDto.email());
        return ResponseEntity.ok(jwtResponseDto);
    }
}