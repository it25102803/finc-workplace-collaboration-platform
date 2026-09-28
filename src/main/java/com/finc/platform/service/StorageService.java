package com.finc.platform.service;

import org.springframework.web.multipart.MultipartFile;
public interface StorageService {

    String store(MultipartFile data, String suggestedName);
    byte[] read(String storagePath);
    void delete(String storagePath);
}
