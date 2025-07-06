package com.fixit.FixIt.Backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file, String userId);
    void deleteFile(String filePath);
} 