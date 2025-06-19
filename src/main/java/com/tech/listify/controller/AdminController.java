package com.tech.listify.controller;

import com.tech.listify.service.HitCounterService;
import com.tech.listify.service.LogGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Административные функции (требуется роль ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final LogGenerationService logGenerationService;
    private final HitCounterService hitCounterService;
    private final Path logDirectory = Paths.get("./logs/archived");

    @Operation(summary = "Скачать архивный лог-файл за определенную дату")
    @GetMapping("/logs/download")
    public ResponseEntity<Resource> downloadLog(
            @Parameter(description = "Дата в формате yyyy-MM-dd", required = true, example = "2025-06-18")
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
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получить статистику посещений по URL")
    @GetMapping("/hits")
    public ResponseEntity<Map<String, Long>> getHitStatistics() {
        return ResponseEntity.ok(hitCounterService.getAllHits());
    }

    @Operation(summary = "Запустить асинхронную задачу генерации отчета по логам")
    @PostMapping("/logs/tasks/generate")
    public ResponseEntity<Map<String, String>> generateLogReportAsync(
            @Parameter(description = "Дата в формате yyyy-MM-dd (опционально, если не указана - логи за все время)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            String taskId = UUID.randomUUID().toString();

            CompletableFuture<Void> future = logGenerationService.generateLogReport(taskId, Optional.ofNullable(date));

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Async log report task with ID '{}' failed to execute.", taskId, ex);
                } else {
                    log.info("Async log report task with ID '{}' completed successfully (or with status NOT_FOUND).", taskId);
                }
            });

            // ИЗМЕНЕНИЕ: Сразу возвращаем taskId клиенту
            log.info("Accepted log report task with ID: {}", taskId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "taskId", taskId,
                    "message", "Задача по генерации отчета принята в обработку.",
                    "statusUrl", "/api/admin/logs/tasks/" + taskId + "/status"
            ));

        } catch (Exception e) {
            log.error("Failed to submit log generation task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Не удалось запустить задачу по генерации отчета."
            ));
        }
    }

    @Operation(summary = "Проверить статус асинхронной задачи")
    @GetMapping("/logs/tasks/{taskId}/status")
    public ResponseEntity<Map<String, Object>> getAsyncTaskStatus(
            @Parameter(description = "UUID задачи", required = true) @PathVariable String taskId) {
        LogGenerationService.TaskStatus status = logGenerationService.getTaskStatus(taskId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("taskId", taskId, "status", status));
    }

    @Operation(summary = "Скачать результат выполнения асинхронной задачи")
    @GetMapping("/logs/tasks/{taskId}/download")
    public ResponseEntity<Resource> downloadGeneratedLog(
            @Parameter(description = "UUID задачи", required = true) @PathVariable String taskId) {

        if (logGenerationService.getTaskStatus(taskId) != LogGenerationService.TaskStatus.SUCCESS) {
            return ResponseEntity.status(404).body(null);
        }
        try {
            Path file = logGenerationService.getLogFilePath(taskId);
            if(file == null){
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
            return ResponseEntity.ok().build();
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}