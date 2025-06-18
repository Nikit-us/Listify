package com.tech.listify.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
@Slf4j
public class LogGenerationService {

    public enum TaskStatus { PENDING, SUCCESS, FAILED, NOT_FOUND }

    private final Map<String, TaskStatus> taskStatuses = new ConcurrentHashMap<>();
    private final Map<String, Path> taskFiles = new ConcurrentHashMap<>();
    private final Path tempLogStorage = Paths.get("./temp-logs");
    private final Path mainLogDirectory = Paths.get("./logs");
    private final Path archivedLogDirectory = mainLogDirectory.resolve("archived");

    @Async
    // ИЗМЕНЕНИЕ: Возвращаемый тип теперь CompletableFuture
    public CompletableFuture<String> generateLogReport(Optional<LocalDate> dateOpt) {
        String taskId = UUID.randomUUID().toString();
        taskStatuses.put(taskId, TaskStatus.PENDING);

        try {
            if (!Files.exists(tempLogStorage)) {
                Files.createDirectories(tempLogStorage);
            }

            List<Path> filesToProcess = findLogFiles(dateOpt);

            if (filesToProcess.isEmpty()) {
                log.warn("No log files found for the specified criteria. Task ID: {}", taskId);
                taskStatuses.put(taskId, TaskStatus.NOT_FOUND);
                return CompletableFuture.completedFuture(taskId);
            }

            String aggregatedContent = aggregateLogContent(filesToProcess);

            Path resultFile = tempLogStorage.resolve(taskId + ".log");
            Files.writeString(resultFile, aggregatedContent);

            taskStatuses.put(taskId, TaskStatus.SUCCESS);
            taskFiles.put(taskId, resultFile);
            log.info("Log report generated successfully. Task ID: {}", taskId);

            // ИЗМЕНЕНИЕ: Используем CompletableFuture.completedFuture() для успешного результата
            return CompletableFuture.completedFuture(taskId);

        } catch (Exception e) {
            log.error("Failed to generate log report for task ID: {}", taskId, e);
            taskStatuses.put(taskId, TaskStatus.FAILED);
            // ИЗМЕНЕНИЕ: Используем failedFuture для проброса исключения
            return CompletableFuture.failedFuture(e);
        }
    }

    public TaskStatus getTaskStatus(String taskId) {
        return taskStatuses.get(taskId);
    }

    public Path getLogFilePath(String taskId) {
        return taskFiles.get(taskId);
    }

    private List<Path> findLogFiles(Optional<LocalDate> dateOpt) throws IOException {
        List<Path> filesToProcess = new ArrayList<>();
        if (dateOpt.isPresent()) {
            LocalDate date = dateOpt.get();
            String logFileName = "listify_" + date.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".log";
            Path sourceLogFile = archivedLogDirectory.resolve(logFileName);
            if (Files.exists(sourceLogFile)) {
                filesToProcess.add(sourceLogFile);
            }
        } else {
            Path currentLog = mainLogDirectory.resolve("listify.log");
            if (Files.exists(currentLog)) {
                filesToProcess.add(currentLog);
            }
            if (Files.exists(archivedLogDirectory)) {
                try (Stream<Path> walk = Files.walk(archivedLogDirectory)) {
                    filesToProcess.addAll(walk.filter(Files::isRegularFile).toList());
                }
            }
        }
        return filesToProcess;
    }

    private String aggregateLogContent(List<Path> files) throws IOException {
        StringBuilder resultBuilder = new StringBuilder();
        for (Path logFile : files) {
            resultBuilder.append("--- Start of ").append(logFile.getFileName()).append(" ---\n");
            try (Stream<String> lines = Files.lines(logFile)) {
                lines.forEach(line -> resultBuilder.append(line).append(System.lineSeparator()));
            }
            resultBuilder.append("--- End of ").append(logFile.getFileName()).append(" ---\n\n");
        }
        return resultBuilder.toString();
    }
}