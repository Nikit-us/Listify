package com.tech.listify.controller;

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
import java.util.Map;

@RestController
@RequestMapping("api/users/")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "API для управления профилями пользователей")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Получить публичный профиль пользователя по ID",
            description = "Возвращает публичную информацию о пользователе. Доступно всем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль пользователя найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getProfile(@Parameter(description = "ID пользователя", required = true, example = "1") @PathVariable Long userId) {
        log.debug("Received request for user profile ID: {}", userId);
        UserProfileDto userProfile = userService.getUserProfileById(userId);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Get current profile: {}", userEmail);
        UserProfileDto userProfileDto = userService.getCurrentUserProfile(userEmail);
        return ResponseEntity.ok(userProfileDto);
    }

    @Operation(summary = "Обновить профиль текущего пользователя",
            description = "Обновляет данные профиля (имя, телефон, город) и опционально аватар текущего аутентифицированного пользователя. " +
                    "Данные профиля передаются как JSON часть 'profile', аватар (если есть) как файловая часть 'avatar'.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль успешно обновлен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь или город не найден", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateProfile(@Valid @RequestPart(value = "profile", required = false) @Schema(description = "Данные для обновления профиля в формате JSON") UserUpdateProfileDto updateDto,
                                                        @RequestPart(value = "avatar", required = false) @Schema(description = "Новый файл аватара (опционально, заменяет старый)") MultipartFile avatarFile,
                                                        Authentication authentication) throws IOException {
        String userEmail = authentication.getName();
        log.info("Received request to update profile for user: {}", userEmail);
        UserProfileDto updatedProfile = userService.updateUserProfile(userEmail, updateDto, avatarFile);
        return ResponseEntity.ok(updatedProfile);
    }
}
