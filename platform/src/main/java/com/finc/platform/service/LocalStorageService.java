package com.finc.platform.service;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service

public class LocalStorageService implements StorageService {

    private final Path root;

    public LocalStorageService(@Value("${app.storage.root:./storage}") String rootDir) {
        this.root = Path.of(rootDir);
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not initialize storage root: " + root, e);
        }
    }

    @Override
    public String store(MultipartFile data, String suggestedName) {
        String baseName = suggestedName == null ? "file" : Path.of(suggestedName).getFileName().toString();
        String storedName = UUID.randomUUID() + "_" + baseName;
        Path target = root.resolve(storedName);
        try {
            data.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file " + suggestedName, e);
        }
        return target.toString();
    }

    @Override
    public byte[] read(String storagePath) {
        try {
            return Files.readAllBytes(Path.of(storagePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file at " + storagePath, e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(Path.of(storagePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file at " + storagePath, e);
        }
    }


}
