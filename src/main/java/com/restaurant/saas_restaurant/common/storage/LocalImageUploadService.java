package com.restaurant.saas_restaurant.common.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalImageUploadService implements ImageUploadService {
    private static final Set<String> ALLOWED_CONTENT_TYPES = 
        Set.of("image/png","image.jpeg","image.jpg","image/webp");
    private static final long MAX_FILES_SIZE_BYTES = 5L * 1024 * 1024; //5MB

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:/uploads}")
    private String baseUrl;

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) 
            throw new IllegalArgumentException("File is empty");
        if (file.getSize() > MAX_FILES_SIZE_BYTES) 
            throw new IllegalArgumentException("File exceeds max size of 5MB");
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType()))
            throw new IllegalArgumentException("Unsupported file type: "+file.getContentType());
    }
    private String extractExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains("."))
            return "";
        return originalFileName.substring(originalFileName.lastIndexOf('.'));
    }
    @Override
    public String upload(MultipartFile file, String subfolder) {
        validate(file);
        try {
            String extension = extractExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + extension;

            Path targetDir = Paths.get(uploadDir, subfolder).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            Path targerFile = targetDir.resolve(fileName).normalize();
            if (!targerFile.startsWith(targetDir))
                throw new IllegalArgumentException("Invalid file name");
            file.transferTo(targerFile);
            return baseUrl + "/" + subfolder + "/" + fileName;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store the image",e);
        }
    }
    @Override
    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            String relative = imageUrl.replaceFirst("^" + baseUrl + "/","");
            Path target = Paths.get(uploadDir,relative).toAbsolutePath().normalize();
            Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!target.startsWith(root)) return;
            Files.deleteIfExists(target);
        } catch (IOException e) {
            //log and swallow a missing/undeletable file shouldn't break the calling transaction
        }
    }
}
