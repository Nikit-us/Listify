package com.tech.listify.service.impl; // Убедись, что пакет правильный

import com.tech.listify.exception.FileStorageException;
import com.tech.listify.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageServiceImpl.class);

    @Value("${upload.path}")
    private String baseUploadPathConfig; // Имя поля, которое будет инжектиться Spring

    private static final String AVATARS_SUBDIR = "avatars";
    private static final String ADS_IMAGES_SUBDIR = "ads";
    private static final String DEFAULT_SUBDIR = "others";

    private Path basePath; // Абсолютный, нормализованный путь для работы

    @PostConstruct
    public void init() {
        try {
            this.basePath = Paths.get(this.baseUploadPathConfig).toAbsolutePath().normalize();
            Files.createDirectories(this.basePath);
            log.info("Base upload directory initialized at: {}", this.basePath);
        } catch (Exception ex) {
            log.error("Could not create or access the base upload directory: {}", this.baseUploadPathConfig, ex);
            throw new FileStorageException("Не удалось инициализировать директорию для загрузки файлов: " + this.baseUploadPathConfig, ex);
        }
    }

    @Override
    public String saveFile(MultipartFile file, String contentType) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("Attempted to save a null or empty file.");
            throw new FileStorageException("Файл не предоставлен или пуст.");
        }

        String originalClientFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String filenameOnly = StringUtils.getFilename(originalClientFilename);

        if (filenameOnly == null || filenameOnly.isEmpty()) {
            filenameOnly = "file_" + UUID.randomUUID().toString().substring(0, 8);
            log.warn("Original filename was empty or invalid after cleaning, using default: {}", filenameOnly);
        }

        String subDir;
        if ("avatar".equalsIgnoreCase(contentType)) {
            subDir = AVATARS_SUBDIR;
        } else if ("ad_image".equalsIgnoreCase(contentType)) {
            subDir = ADS_IMAGES_SUBDIR;
        } else {
            log.warn("Unknown content type '{}', using default subdirectory '{}'", contentType, DEFAULT_SUBDIR);
            subDir = DEFAULT_SUBDIR;
        }

        Path targetSubDir = this.basePath.resolve(subDir).normalize();

        if (!Files.exists(targetSubDir)) {
            try {
                Files.createDirectories(targetSubDir);
                log.info("Created subdirectory for content type '{}': {}", contentType, targetSubDir);
            } catch (IOException ex) {
                log.error("Could not create subdirectory: {}", targetSubDir, ex);
                throw new IOException("Не удалось создать поддиректорию для типа " + contentType, ex);
            }
        }

        String uuidFile = UUID.randomUUID().toString();
        String fileExtension = "";
        int dotIndex = filenameOnly.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filenameOnly.length() - 1) {
            fileExtension = filenameOnly.substring(dotIndex);
        } else {
            log.warn("Could not determine file extension for original filename: '{}' (cleaned: '{}'). Saving without extension or with guessed.", originalClientFilename, filenameOnly);
        }

        String resultFilename = uuidFile + fileExtension;
        Path targetFilePath = targetSubDir.resolve(resultFilename).normalize();

        if (!targetFilePath.startsWith(this.basePath)) {
            log.error("Security alert: Attempt to store file outside base upload directory. Calculated target: {}, Base: {}", targetFilePath, this.basePath);
            throw new FileStorageException("Невозможно сохранить файл вне основной директории загрузок: " + originalClientFilename);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store file '{}' (saved as '{}'): {}", originalClientFilename, resultFilename, e.getMessage(), e);
            throw new IOException("Не удалось сохранить файл " + originalClientFilename, e);
        }

        String relativeUrl = "/uploads/" + subDir + "/" + resultFilename;
        log.info("File '{}' saved successfully as '{}'", originalClientFilename, relativeUrl);
        return relativeUrl;
    }

    @Override
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isBlank()) {
            log.warn("Attempted to delete file with null or blank URL.");
            return;
        }

        if (!fileUrl.startsWith("/uploads/")) {
            log.warn("Attempted to delete file with invalid URL prefix: {}", fileUrl);
            return;
        }

        try {
            String relativePath = fileUrl.substring("/uploads/".length());
            Path filePath = this.basePath.resolve(relativePath).normalize();

            if (!filePath.startsWith(this.basePath)) {
                log.error("Security alert: Attempted to delete file outside base upload directory: {}. Calculated path: {}", fileUrl, filePath);
                throw new IOException("Недопустимый путь к файлу для удаления.");
            }

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Successfully deleted file: {}", filePath);
            } else {
                log.warn("File not found for deletion (or already deleted): {}", filePath);
            }

        } catch (InvalidPathException e) {
            log.error("Invalid path syntax for deletion: {}", fileUrl, e);
            throw new IOException("Некорректный синтаксис пути файла для удаления: " + fileUrl, e);
        } catch (DirectoryNotEmptyException e) {
            log.error("Attempted to delete a non-empty directory as a file: {}", fileUrl, e);
            throw new IOException("Невозможно удалить непустую директорию как файл: " + fileUrl, e);
        } catch (IOException e) {
            log.error("Could not delete file: {}. Error: {}", fileUrl, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing file path for deletion: {}", fileUrl, e);
            throw new IOException("Ошибка при обработке пути файла для удаления: " + fileUrl, e);
        }
    }
}