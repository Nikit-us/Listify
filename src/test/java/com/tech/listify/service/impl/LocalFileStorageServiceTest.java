package com.tech.listify.service.impl; // Убедись, что пакет правильный

import com.tech.listify.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDirRoot; // JUnit создает и удаляет эту директорию

    private LocalFileStorageServiceImpl fileStorageService; // Тестируем реализацию

    private final String AVATARS_SUBDIR = "avatars";
    private final String ADS_IMAGES_SUBDIR = "ads";
    private final String OTHERS_SUBDIR = "others";

    private Path baseUploadPathForTest;


    @BeforeEach
    void setUp() throws IOException {
        baseUploadPathForTest = tempDirRoot.resolve("uploads_test_root"); // Используем уникальное имя для базовой директории теста
        Files.createDirectories(baseUploadPathForTest);

        fileStorageService = new LocalFileStorageServiceImpl();
        // Устанавливаем значение поля baseUploadPathConfig, которое обычно инжектится через @Value
        ReflectionTestUtils.setField(fileStorageService, "baseUploadPathConfig", baseUploadPathForTest.toString());
        // Вызываем init() вручную, так как @PostConstruct не работает в unit-тестах без Spring контекста
        fileStorageService.init();
    }

    @Test
    @DisplayName("init - создает базовую директорию, если ее нет")
    void init_shouldCreateBaseDirectory_ifNotExists() {
        assertThat(Files.exists(baseUploadPathForTest)).isTrue();
        // Дополнительно можно проверить, что init не падает, если директория уже есть (повторный вызов)
        fileStorageService.init(); // Должен пройти без ошибок
        assertThat(Files.exists(baseUploadPathForTest)).isTrue();
    }

    @Test
    @DisplayName("saveFile - успешно сохраняет файл аватара в поддиректорию 'avatars'")
    void saveFile_whenAvatarType_shouldSaveToAvatarsSubdir() throws IOException {
        String filename = "test-avatar.png";
        MultipartFile multipartFile = new MockMultipartFile(
                "avatarFile",
                filename,
                "image/png",
                "avatar content".getBytes(StandardCharsets.UTF_8)
        );

        String savedFileUrl = fileStorageService.saveFile(multipartFile, "avatar");

        assertThat(savedFileUrl).startsWith("/uploads/avatars/");
        assertThat(savedFileUrl).endsWith(".png");
        String storedFilename = savedFileUrl.substring(("/uploads/" + AVATARS_SUBDIR + "/").length());
        Path expectedFilePath = baseUploadPathForTest.resolve(AVATARS_SUBDIR).resolve(storedFilename); // Используем baseUploadPathForTest
        assertThat(Files.exists(expectedFilePath)).isTrue();
        assertThat(Files.readString(expectedFilePath)).isEqualTo("avatar content");
    }

    @Test
    @DisplayName("saveFile - успешно сохраняет файл объявления в поддиректорию 'ads'")
    void saveFile_whenAdImageType_shouldSaveToAdsSubdir() throws IOException {
        String filename = "test-ad.jpg";
        MultipartFile multipartFile = new MockMultipartFile(
                "adImageFile",
                filename,
                "image/jpeg",
                "ad content".getBytes(StandardCharsets.UTF_8)
        );

        String savedFileUrl = fileStorageService.saveFile(multipartFile, "ad_image");

        assertThat(savedFileUrl).startsWith("/uploads/ads/");
        assertThat(savedFileUrl).endsWith(".jpg");
        String storedFilename = savedFileUrl.substring(("/uploads/" + ADS_IMAGES_SUBDIR + "/").length());
        Path expectedFilePath = baseUploadPathForTest.resolve(ADS_IMAGES_SUBDIR).resolve(storedFilename);
        assertThat(Files.exists(expectedFilePath)).isTrue();
        assertThat(Files.readString(expectedFilePath)).isEqualTo("ad content");
    }

    @Test
    @DisplayName("saveFile - пустой файл - бросает FileStorageException")
    void saveFile_whenFileIsEmpty_shouldThrowFileStorageException() {
        MultipartFile emptyFile = new MockMultipartFile(
                "emptyFile",
                "empty.txt",
                "text/plain",
                new byte[0] // Пустой контент
        );
        assertThatThrownBy(() -> fileStorageService.saveFile(emptyFile, "other"))
                .isInstanceOf(FileStorageException.class)
                .hasMessage("Файл не предоставлен или пуст.");
    }

    @Test
    @DisplayName("saveFile - null файл - бросает FileStorageException")
    void saveFile_whenFileIsNull_shouldThrowFileStorageException() {
        assertThatThrownBy(() -> fileStorageService.saveFile(null, "other"))
                .isInstanceOf(FileStorageException.class)
                .hasMessage("Файл не предоставлен или пуст.");
    }


    @Test
    @DisplayName("saveFile - имя файла содержит '../' - должен сохраниться только сам файл без пути (с UUID и расширением)")
    void saveFile_whenOriginalFilenameContainsRelativePath_shouldSaveOnlyBaseFilenameWithUUID() throws IOException {
        String maliciousOriginalFilename = "../../etc/passwd.txt";
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                maliciousOriginalFilename,
                "text/plain",
                "content".getBytes(StandardCharsets.UTF_8)
        );

        String savedFileUrl = fileStorageService.saveFile(multipartFile, OTHERS_SUBDIR);

        assertThat(savedFileUrl).startsWith("/uploads/" + OTHERS_SUBDIR + "/");
        assertThat(savedFileUrl).endsWith(".txt"); // Проверяем, что расширение извлечено правильно
        assertThat(savedFileUrl).doesNotContain(".."); // Убедимся, что ../ нет в URL
        assertThat(savedFileUrl.substring(("/uploads/" + OTHERS_SUBDIR + "/").length())).doesNotContain("passwd"); // Имени "passwd" там не будет
        assertThat(savedFileUrl.substring(("/uploads/" + OTHERS_SUBDIR + "/").length()).length())
                .isEqualTo(UUID.randomUUID().toString().length() + ".txt".length()); // Проверяем длину UUID + .txt

        String storedFilenameOnly = savedFileUrl.substring(("/uploads/" + OTHERS_SUBDIR + "/").length());
        Path expectedFilePath = baseUploadPathForTest.resolve(OTHERS_SUBDIR).resolve(storedFilenameOnly);
        assertThat(Files.exists(expectedFilePath)).isTrue();
        assertThat(Files.readString(expectedFilePath)).isEqualTo("content");
    }

    @Test
    @DisplayName("saveFile - файл без расширения - сохраняется без расширения (но с UUID)")
    void saveFile_whenNoExtension_shouldSaveWithoutExtensionButWithUUID() throws IOException {
        MultipartFile noExtFile = new MockMultipartFile("noExt", "filewithoutextension", "application/octet-stream", "content".getBytes());
        String savedUrl = fileStorageService.saveFile(noExtFile, OTHERS_SUBDIR);

        assertThat(savedUrl).startsWith("/uploads/" + OTHERS_SUBDIR + "/");
        String filenameInUrl = savedUrl.substring(("/uploads/" + OTHERS_SUBDIR + "/").length());
        assertThat(filenameInUrl).doesNotContain("."); // Проверяем, что нет точки, если не было расширения
        assertThat(filenameInUrl.length()).isGreaterThanOrEqualTo(UUID.randomUUID().toString().length()); // Проверяем, что это UUID
    }

    @Test
    @DisplayName("deleteFile - успешно удаляет существующий файл")
    void deleteFile_whenFileExists_shouldDeleteFile() throws IOException {
        String filename = "to-delete.txt";
        Path subdir = baseUploadPathForTest.resolve(ADS_IMAGES_SUBDIR);
        Files.createDirectories(subdir);
        Path filePath = subdir.resolve(filename);
        Files.writeString(filePath, "content");
        String fileUrl = "/uploads/" + ADS_IMAGES_SUBDIR + "/" + filename;
        assertThat(Files.exists(filePath)).isTrue();

        fileStorageService.deleteFile(fileUrl);

        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    @DisplayName("deleteFile - некорректный URL (не /uploads/) - ничего не делает, не бросает ошибку")
    void deleteFile_whenInvalidUrlPrefix_shouldDoNothingAndNotThrow() throws IOException {
        String invalidUrl = "/someotherpath/file.txt";
        fileStorageService.deleteFile(invalidUrl);
    }



    @Test
    @DisplayName("deleteFile - пытается удалить несуществующий файл - не бросает ошибку, возвращает void")
    void deleteFile_whenFileDoesNotExist_shouldNotThrowException() throws IOException {
        String fileUrl = "/uploads/ads/non-existent.txt";
        fileStorageService.deleteFile(fileUrl);
    }

    @Test
    @DisplayName("deleteFile - попытка удалить файл вне базовой директории (через ../ в URL) - бросает IOException")
    void deleteFile_whenUrlTriesToEscapeBaseDir_shouldThrowIOException() {
        String maliciousFileUrl = "/uploads/../secret.txt";

        assertThatThrownBy(() -> fileStorageService.deleteFile(maliciousFileUrl))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Недопустимый путь к файлу для удаления.");
    }

    @Test
    @DisplayName("deleteFile - null или пустой URL - ничего не делает, не бросает ошибку")
    void deleteFile_whenUrlIsNullOrEmpty_shouldDoNothingAndNotThrow() throws IOException {
        fileStorageService.deleteFile(null);
        fileStorageService.deleteFile("");
        fileStorageService.deleteFile("   ");
    }
}