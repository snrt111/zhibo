package com.zhibo.backend.controller;

import com.zhibo.backend.service.LocalStorageService;
import com.zhibo.backend.service.MinioStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Value("${upload.path:/app/uploads}")
    private String uploadPath;

    @Value("${upload.use-minio:false}")
    private boolean useMinio;

    @Autowired(required = false)
    private MinioStorageService minioStorageService;

    @Autowired(required = false)
    private LocalStorageService localStorageService;

    @GetMapping("/avatar/{userId}")
    public ResponseEntity<Void> getAvatar(@PathVariable Long userId) {
        String cachedUrl = null;
        if (useMinio && minioStorageService != null) {
            cachedUrl = minioStorageService.getCachedAvatarUrl(userId);
        } else if (localStorageService != null) {
            cachedUrl = localStorageService.getCachedAvatarUrl(userId);
        }
        if (cachedUrl != null) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, cachedUrl)
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                    .build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        try {
            Path imagePath = Paths.get(uploadPath, fileName);
            File imageFile = imagePath.toFile();

            if (!imageFile.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(imageFile);
            String contentType = Files.probeContentType(imagePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
