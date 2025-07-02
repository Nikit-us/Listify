package com.tech.listify.controller;

import com.tech.listify.dto.ApiErrorResponse;
import com.tech.listify.dto.userdto.UserProfileDto;
import com.tech.listify.dto.userdto.UserUpdateProfileDto;
import com.tech.listify.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "API для управления профилями пользователей")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Получить публичный профиль пользователя по ID",
            description = "Возвращает публичную информацию о пользователе, включая его имя, город, дату регистрации и количество активных объявлений. Доступно всем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль пользователя найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь с указанным ID не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getProfile(
            @Parameter(description = "ID пользователя, чей профиль нужно получить", required = true, example = "1")
            @PathVariable Long userId) {
        log.debug("Received request for user profile ID: {}", userId);
        UserProfileDto userProfile = userService.getUserProfileById(userId);
        return ResponseEntity.ok(userProfile);
    }

    @Operation(summary = "Получить профиль текущего пользователя",
            description = "Возвращает полную информацию о профиле аутентифицированного пользователя. Требуется аутентификация.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль успешно получен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Get current profile: {}", userEmail);
        UserProfileDto userProfileDto = userService.getCurrentUserProfile(userEmail);
        return ResponseEntity.ok(userProfileDto);
    }

    @Operation(summary = "Обновить профиль текущего пользователя",
            description = "Обновляет данные профиля (имя, телефон, город) и опционально аватар текущего аутентифицированного пользователя. " +
                    "Запрос должен быть в формате `multipart/form-data` и содержать: " +
                    "1. `profile`: JSON-объект с данными для обновления (`UserUpdateProfileDto`). " +
                    "2. `avatar`: Файл нового аватара (опционально).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль успешно обновлен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь или указанный город не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера (например, при сохранении файла)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileDto> updateProfile(
            @Parameter(description = "Данные для обновления профиля в формате JSON. Можно отправлять только изменяемые поля.")
            @Valid @RequestPart(value = "profile", required = false) UserUpdateProfileDto updateDto,
            @Parameter(description = "Новый файл аватара (заменяет старый, если он был).")
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile,
            Authentication authentication) throws IOException {
        String userEmail = authentication.getName();
        log.info("Received request to update profile for user: {}", userEmail);
        UserProfileDto updatedProfile = userService.updateUserProfile(userEmail, updateDto, avatarFile);
        return ResponseEntity.ok(updatedProfile);
    }
}