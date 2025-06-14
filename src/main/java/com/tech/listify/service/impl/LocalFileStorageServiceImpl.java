package com.tech.listify.service.impl;

import com.tech.listify.exception.FileStorageException;
import com.tech.listify.exception.FileStorageException.ErrorType;
import com.tech.listify.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageServiceImpl.class);

    @Value("${upload.path}")
    private String baseUploadPathConfig;

    @Value("${upload.allowed-mime-types}")
    private List<String> allowedMimeTypes;

    private static final String AVATARS_SUBDIR = "avatars";
    private static final String ADS_IMAGES_SUBDIR = "ads";
    private static final String DEFAULT_SUBDIR = "others";

    private Path basePath;

    @PostConstruct
    public void init() {
        try {
            this.basePath = Paths.get(this.baseUploadPathConfig).toAbsolutePath().normalize();
            Files.createDirectories(this.basePath);
            log.info("Base upload directory initialized at: {}", this.basePath);
        } catch (Exception ex) {
            log.error("Could not create or access the base upload directory: {}", this.baseUploadPathConfig, ex);
            throw new FileStorageException("Не удалось инициализировать директорию для загрузки файлов: " + this.baseUploadPathConfig, ex, ErrorType.SERVER_ERROR);
        }
    }

    @Override
    public String saveFile(MultipartFile file, String contentType) throws IOException {
        validateFile(file);

        String subDir = determineSubdirectory(contentType);
        Path targetSubDir = createSubdirectoryIfNotExists(subDir);

        String resultFilename = generateUniqueFilename(file.getOriginalFilename());
        Path targetFilePath = targetSubDir.resolve(resultFilename).normalize();

        ensureSafePath(targetFilePath, file.getOriginalFilename());

        copyFileToDisk(file, targetFilePath, file.getOriginalFilename(), resultFilename);

        String relativeUrl = "/uploads/" + subDir + "/" + resultFilename;
        log.info("File '{}' saved successfully as '{}'", file.getOriginalFilename(), relativeUrl);
        return relativeUrl;
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("Attempted to save a null or empty file.");
            throw new FileStorageException("Файл не предоставлен или пуст.", ErrorType.CLIENT_ERROR);
        }
        Tika tika = new Tika();
        String detectedMimeType = tika.detect(file.getInputStream());
//        if (!allowedMimeTypes.contains(detectedMimeType)) {
//            log.error("File upload rejected. Detected MIME type '{}' is not allowed. Original filename: {}",
//                    detectedMimeType, file.getOriginalFilename());
//            throw new FileStorageException("Недопустимый тип файла. Разрешены только изображения (JPEG, PNG, GIF).", ErrorType.CLIENT_ERROR);
//        }
    }

    private String determineSubdirectory(String contentType) {
        if ("avatar".equalsIgnoreCase(contentType)) {
            return AVATARS_SUBDIR;
        } else if ("ad_image".equalsIgnoreCase(contentType)) {
            return ADS_IMAGES_SUBDIR;
        } else {
            log.warn("Unknown content type '{}', using default subdirectory '{}'", contentType, DEFAULT_SUBDIR);
            return DEFAULT_SUBDIR;
        }
    }

    private Path createSubdirectoryIfNotExists(String subDir) throws FileStorageException {
        Path targetSubDir = this.basePath.resolve(subDir).normalize();
        if (!Files.exists(targetSubDir)) {
            try {
                Files.createDirectories(targetSubDir);
                log.info("Created subdirectory: {}", targetSubDir);
            } catch (IOException ex) {
                log.error("Could not create subdirectory: {}", targetSubDir, ex);
                throw new FileStorageException("Не удалось создать поддиректорию для типа " + subDir, ex, ErrorType.SERVER_ERROR);
            }
        }
        return targetSubDir;
    }

    private String generateUniqueFilename(String originalClientFilename) {
        String cleanedFilename = originalClientFilename != null ? StringUtils.cleanPath(originalClientFilename) : "";
        String filenameOnly = StringUtils.getFilename(cleanedFilename);

        if (!StringUtils.hasText(filenameOnly)) {
            filenameOnly = "file_" + UUID.randomUUID().toString().substring(0, 8);
        }

        String uuidFile = UUID.randomUUID().toString();
        String fileExtension = "";
        int dotIndex = filenameOnly.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filenameOnly.length() - 1) {
            fileExtension = filenameOnly.substring(dotIndex);
        }
        return uuidFile + fileExtension;
    }

    private void ensureSafePath(Path targetFilePath, String originalFilename) throws FileStorageException {
        if (!targetFilePath.startsWith(this.basePath)) {
            log.error("Security alert: Attempt to store file outside base upload directory. Calculated target: {}, Base: {}", targetFilePath, this.basePath);
            throw new FileStorageException("Невозможно сохранить файл вне основной директории загрузок: " + originalFilename, ErrorType.CLIENT_ERROR);
        }
    }

    private void copyFileToDisk(MultipartFile file, Path targetFilePath, String originalFilename, String resultFilename) throws FileStorageException {
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store file '{}' (saved as '{}'): {}", originalFilename, resultFilename, e.getMessage(), e);
            throw new FileStorageException("Не удалось сохранить файл " + originalFilename, e, ErrorType.SERVER_ERROR);
        }
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

            Files.deleteIfExists(filePath);

        } catch (InvalidPathException | IOException e) {
            log.error("Could not delete file: {}. Error: {}", fileUrl, e.getMessage());
            throw new IOException("Ошибка при удалении файла: " + fileUrl, e);
        }
    }
}