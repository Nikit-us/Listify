package com.tech.listify.controller;

import com.tech.listify.dto.ApiErrorResponse; // Убедитесь, что этот импорт правильный
import com.tech.listify.service.impl.HitCounterService;
import com.tech.listify.service.impl.LogGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Административные функции (требуется роль ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final LogGenerationService logGenerationService;
    private final HitCounterService hitCounterService;
    private final Path logDirectory = Paths.get("./logs/archived");

    @Operation(summary = "Скачать архивный лог-файл",
            description = "Позволяет администратору скачать архивный лог-файл за определенную дату.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Лог-файл успешно скачан",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат даты",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Лог-файл за указанную дату не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/logs/download")
    public ResponseEntity<Resource> downloadLog(
            @Parameter(description = "Дата лог-файла в формате YYYY-MM-DD", required = true, example = "2025-06-18")
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Path file = logDirectory.resolve("listify_" + date.toString() + ".log").normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                log.warn("Log file not found for date: {}", date);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("Malformed URL for log file on date {}", date, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получить статистику посещений URL",
            description = "Возвращает карту, где ключ - это URL, а значение - количество посещений.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статистика успешно получена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object",
                                    additionalProperties = Schema.AdditionalPropertiesValue.TRUE,
                                    example = "{\"/api/ads/1\": 150, \"/api/ads/search\": 230}"))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/hits")
    public ResponseEntity<Map<String, Long>> getHitStatistics() {
        return ResponseEntity.ok(hitCounterService.getAllHits());
    }

    @Operation(summary = "Запустить асинхронную генерацию отчета по логам",
            description = "Принимает задачу на формирование общего лог-файла. Если дата не указана, обрабатываются все логи. " +
                    "Возвращает ID задачи для отслеживания статуса.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Задача принята в обработку",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"taskId\":\"d290f1ee-6c54-4b01-90e6-d701748f0851\",\"message\":\"Задача по генерации отчета принята в обработку.\",\"statusUrl\":\"/api/admin/logs/tasks/d290f1ee-6c54-4b01-90e6-d701748f0851/status\"}"))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Не удалось запустить задачу",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/logs/tasks/generate")
    public ResponseEntity<Map<String, String>> generateLogReportAsync(
            @Parameter(description = "Дата в формате YYYY-MM-DD. Если не указана, логи собираются за все время.", example = "2025-07-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String taskId = UUID.randomUUID().toString();
        logGenerationService.generateLogReport(taskId, Optional.ofNullable(date));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "taskId", taskId,
                "message", "Задача по генерации отчета принята в обработку.",
                "statusUrl", "/api/admin/logs/tasks/" + taskId + "/status"
        ));
    }

    @Operation(summary = "Проверить статус задачи генерации логов",
            description = "Возвращает текущий статус асинхронной задачи: PENDING, SUCCESS, FAILED, или NOT_FOUND.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус получен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"taskId\":\"d290f1ee-6c54-4b01-90e6-d701748f0851\",\"status\":\"SUCCESS\"}"))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Задача с таким ID не найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/logs/tasks/{taskId}/status")
    public ResponseEntity<Map<String, Object>> getAsyncTaskStatus(
            @Parameter(description = "Уникальный идентификатор (UUID) задачи", required = true, example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
            @PathVariable String taskId) {
        LogGenerationService.TaskStatus status = logGenerationService.getTaskStatus(taskId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("taskId", taskId, "status", status));
    }

    @Operation(summary = "Скачать сгенерированный отчет по логам",
            description = "Скачивает итоговый лог-файл, созданный асинхронной задачей. Доступно только если статус задачи - SUCCESS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл отчета успешно скачан",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Задача не найдена, или ее выполнение еще не завершено успешно.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/logs/tasks/{taskId}/download")
    public ResponseEntity<Resource> downloadGeneratedLog(
            @Parameter(description = "Уникальный идентификатор (UUID) задачи", required = true, example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
            @PathVariable String taskId) {
        if (logGenerationService.getTaskStatus(taskId) != LogGenerationService.TaskStatus.SUCCESS) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            Path file = logGenerationService.getLogFilePath(taskId);
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}