package com.tech.listify.service.impl;

import com.tech.listify.exception.FileStorageException;
import com.tech.listify.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {


    @Value("${upload.path}")
    private String baseUploadPath;


    private static final String AVATARS_SUBDIR = "avatars";
    private static final String ADS_IMAGES_SUBDIR = "ads";
    private static final String DEFAULT_SUBDIR = "others";

    private Path basePath;

    @PostConstruct
    public void init() {
        try {
            basePath = Paths.get(baseUploadPath).toAbsolutePath().normalize();
            Files.createDirectories(basePath);
            log.info("Base upload directory initialized: {}", basePath);
        } catch (Exception ex) {
            log.error("Could not create the upload directory: {}", baseUploadPath, ex);
            throw new FileStorageException("Не удалось инициализировать директорию для загрузки файлов.", ex);
        }
    }

    @Override
    public String saveFile(MultipartFile file, String contentType) throws IOException {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
            throw new FileStorageException("Файл не предоставлен или пуст");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String subDir;

        // Определяем поддиректорию
        if ("avatar".equalsIgnoreCase(contentType)) {
            subDir = AVATARS_SUBDIR;
        } else if ("ad_image".equalsIgnoreCase(contentType)) {
            subDir = ADS_IMAGES_SUBDIR;
        } else {
            log.warn("Unknown content type '{}', using default subdirectory '{}'", contentType, DEFAULT_SUBDIR);
            subDir = DEFAULT_SUBDIR;
        }

        // Формируем путь к поддиректории
        Path targetSubDir = basePath.resolve(subDir).normalize();

        // Создаем поддиректорию, если ее нет
        if (!Files.exists(targetSubDir)) {
            try {
                Files.createDirectories(targetSubDir);
                log.info("Created subdirectory: {}", targetSubDir);
            } catch (IOException ex) {
                log.error("Could not create subdirectory: {}", targetSubDir, ex);
                throw new IOException("Не удалось создать поддиректорию для типа " + contentType, ex);
            }
        }

        // Генерируем уникальное имя файла
        String uuidFile = UUID.randomUUID().toString();
        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            fileExtension = originalFilename.substring(dotIndex);
        } else {
            log.warn("Could not determine file extension for: {}", originalFilename);
            // Решить: использовать расширение по умолчанию? Выбросить ошибку?
        }
        String resultFilename = uuidFile + fileExtension;
        Path targetFilePath = targetSubDir.resolve(resultFilename);

        // Проверка безопасности (избыточна, если baseUploadPath абсолютный и нормализованный, но не помешает)
        if (!targetFilePath.startsWith(basePath)) {
            throw new FileStorageException("Невозможно сохранить файл вне основной директории загрузок: " + originalFilename);
        }

        // Копируем файл
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store file {}: {}", originalFilename, e.getMessage(), e);
            throw new IOException("Не удалось сохранить файл " + originalFilename, e);
        }

        // Возвращаем относительный URL
        String relativeUrl = "/uploads/" + subDir + "/" + resultFilename; // Префикс /uploads/ совпадает с static-path-pattern
        log.info("File saved successfully: {} as {}", originalFilename, relativeUrl);
        return relativeUrl;
    }

    @Override
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isBlank() || !fileUrl.startsWith("/uploads/")) {
            log.warn("Attempted to delete file with invalid or blank path: {}", fileUrl);
            return; // Ничего не делаем
        }

        try {
            // Извлекаем путь относительно /uploads/
            String relativePath = fileUrl.substring("/uploads/".length());
            Path filePath = basePath.resolve(relativePath).normalize();

            // Проверка безопасности
            if (!filePath.startsWith(basePath)) {
                log.error("Attempted to delete file outside base upload directory: {}", fileUrl);
                throw new IOException("Недопустимый путь к файлу для удаления.");
            }

            Files.deleteIfExists(filePath); // Используем deleteIfExists, чтобы не падать, если файла уже нет
            log.info("Successfully deleted file (if existed): {}", filePath);

        } catch (InvalidPathException e) {
            log.error("Invalid path syntax for deletion: {}", fileUrl, e);
            throw new IOException("Некорректный синтаксис пути файла для удаления: " + fileUrl, e);
        } catch (IOException e) {
            log.error("Could not delete file: {}. Error: {}", fileUrl, e.getMessage());
            throw e; // Пробрасываем ошибку ввода-вывода
        } catch (Exception e) { // Ловим остальные неожиданные ошибки
            log.error("Error processing file path for deletion: {}", fileUrl, e);
            throw new IOException("Ошибка при обработке пути файла для удаления: " + fileUrl, e);
        }
    }
}
