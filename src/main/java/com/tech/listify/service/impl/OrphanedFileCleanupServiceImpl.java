package com.tech.listify.service.impl;

import com.tech.listify.model.AdvertisementImage;
import com.tech.listify.model.User;
import com.tech.listify.repository.AdvertisementImageRepository;
import com.tech.listify.repository.UserRepository;
import com.tech.listify.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrphanedFileCleanupServiceImpl {

    private final UserRepository userRepository;
    private final AdvertisementImageRepository advertisementImageRepository;
    private final FileStorageService fileStorageService;

    @Value("${upload.path}")
    private String uploadPath;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOrphanedFiles() {
        log.info("Starting orphaned files cleanup task...");
        long startTime = System.currentTimeMillis();

        try {
            Set<String> knownUrls = getAllKnownUrls();
            log.debug("Found {} known file URLs in the database.", knownUrls.size());

            Set<Path> filesOnDisk = getAllFilesOnDisk();
            if (filesOnDisk.isEmpty()) {
                log.info("Upload directory is empty. No cleanup needed.");
                return;
            }
            log.debug("Found {} files on disk in the upload directory.", filesOnDisk.size());

            int deletedCount = 0;
            for (Path filePath : filesOnDisk) {
                String fileUrl = convertPathToUrl(filePath);
                if (!knownUrls.contains(fileUrl)) {
                    if (deleteSingleOrphanedFile(filePath, fileUrl)) {
                        deletedCount++;
                    }
                }
            }
            long duration = System.currentTimeMillis() - startTime;
            log.info("Orphaned files cleanup task finished in {} ms. Deleted {} files.", duration, deletedCount);

        } catch (Exception e) {
            log.error("An unexpected error occurred during orphaned file cleanup task.", e);
        }
    }

    /**
     * Пытается удалить один файл-сироту и логирует результат.
     *
     * @return true, если удаление прошло успешно, иначе false.
     */
    private boolean deleteSingleOrphanedFile(Path filePath, String fileUrl) {
        try {
            fileStorageService.deleteFile(fileUrl);
            log.warn("Deleted orphaned file via FileStorageService: {}", filePath);
            return true;
        } catch (IOException e) {
            log.error("Failed to delete orphaned file using FileStorageService: {}", filePath, e);
            return false;
        }
    }

    private Set<String> getAllKnownUrls() {
        Set<String> avatarUrls = userRepository.findAll().stream()
                .map(User::getAvatarUrl)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toSet());

        Set<String> adImageUrls = advertisementImageRepository.findAll().stream()
                .map(AdvertisementImage::getImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toSet());

        Set<String> allUrls = new HashSet<>(avatarUrls);
        allUrls.addAll(adImageUrls);
        return allUrls;
    }

    private Set<Path> getAllFilesOnDisk() throws IOException {
        Path rootPath = Paths.get(this.uploadPath);
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            return Set.of();
        }

        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toSet());
        }
    }

    private String convertPathToUrl(Path absolutePath) {
        Path rootPath = Paths.get(this.uploadPath).toAbsolutePath().normalize();
        Path relativePath = rootPath.relativize(absolutePath.toAbsolutePath().normalize());
        return "/uploads/" + relativePath.toString().replace('\\', '/');
    }
}