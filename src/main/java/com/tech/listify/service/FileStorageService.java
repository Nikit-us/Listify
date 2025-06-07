package com.tech.listify.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String saveFile(MultipartFile file, String contentType) throws IOException;

    void deleteFile(String fileUrl) throws IOException;
}
