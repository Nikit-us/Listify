package com.tech.listify.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {

    /**
     * Сохраняет загруженный файл в соответствующую поддиректорию.
     *
     * @param file Загруженный файл.
     * @param contentType Строковый идентификатор типа контента (например, "avatar", "ad_image").
     * @return Относительный URL-путь к сохраненному файлу (например, /uploads/ads/filename.jpg).
     * @throws IOException Если произошла ошибка ввода/вывода.
     * @throws FileStorageException Если файл некорректен.
     */
    String saveFile(MultipartFile file, String contentType) throws IOException, IOException;

    /**
     * Удаляет файл по его URL-пути.
     *
     * @param fileUrl URL-путь к файлу (например, /uploads/ads/filename.jpg).
     * @throws IOException Если произошла ошибка ввода/вывода при удалении.
     */
    void deleteFile(String fileUrl) throws IOException;
}
