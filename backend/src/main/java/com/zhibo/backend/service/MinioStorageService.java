package com.zhibo.backend.service;

import com.zhibo.backend.config.MinioConfig;
import com.zhibo.backend.exception.BusinessException;
import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "upload.use-minio", havingValue = "true")
public class MinioStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(MinioStorageService.class);

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ImageCompressService imageCompressService;

    private static final String AVATAR_CACHE_KEY = "avatar:url:";
    private static final String FILE_CACHE_KEY = "file:url:";

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        String fileName = generateFileName(file, directory);
        uploadToMinio(file, fileName);
        String fileUrl = buildFileUrl(fileName);
        cacheFileUrl(fileName, fileUrl);
        logger.info("File uploaded: {}", fileName);
        return fileUrl;
    }

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        String fileName = generateAvatarName(file, userId);
        byte[] compressedData = imageCompressService.compressAvatar(file);
        uploadToMinio(compressedData, fileName, file.getContentType());
        String fileUrl = buildFileUrl(fileName);
        cacheAvatarUrl(userId, fileUrl);
        logger.info("Avatar uploaded: {}", fileName);
        return fileUrl;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        String objectName = extractObjectName(fileUrl);
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build()
            );
            redisTemplate.delete(FILE_CACHE_KEY + objectName);
            logger.info("File deleted: {}", objectName);
        } catch (Exception e) {
            logger.error("Failed to delete file: {}", objectName, e);
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        return buildFileUrl(objectName);
    }

    @Override
    public boolean exists(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }
        String objectName = extractObjectName(fileUrl);
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getPresignedUrl(String objectName, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build()
            );
        } catch (Exception e) {
            logger.error("Failed to get presigned url: {}", objectName, e);
            return null;
        }
    }

    public String getCachedAvatarUrl(Long userId) {
        try {
            return (String) redisTemplate.opsForValue().get(AVATAR_CACHE_KEY + userId);
        } catch (Exception e) {
            logger.warn("Failed to get cached avatar url for user: {}", userId, e);
            return null;
        }
    }

    public void cacheAvatarUrl(Long userId, String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(AVATAR_CACHE_KEY + userId, avatarUrl, 7, TimeUnit.DAYS);
            } catch (Exception e) {
                logger.warn("Failed to cache avatar url for user: {}", userId, e);
            }
        }
    }

    private String generateFileName(MultipartFile file, String directory) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("文件名不能为空");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        return directory + "/" + UUID.randomUUID() + suffix;
    }

    private String generateAvatarName(MultipartFile file, Long userId) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("文件名不能为空");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        return "avatar/" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + suffix;
    }

    private void uploadToMinio(MultipartFile file, String fileName) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
        } catch (Exception e) {
            logger.error("Failed to upload to MinIO: {}", fileName, e);
            throw new BusinessException("文件上传失败");
        }
    }

    private void uploadToMinio(byte[] data, String fileName, String contentType) {
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(inputStream, data.length, -1)
                    .contentType(contentType)
                    .build()
            );
        } catch (Exception e) {
            logger.error("Failed to upload to MinIO: {}", fileName, e);
            throw new BusinessException("文件上传失败");
        }
    }

    private String buildFileUrl(String fileName) {
        return minioConfig.getPublicUrl() + "/" + minioConfig.getBucketName() + "/" + fileName;
    }

    private String extractObjectName(String fileUrl) {
        String prefix = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/";
        return fileUrl.replace(prefix, "");
    }

    private void cacheFileUrl(String fileName, String fileUrl) {
        try {
            redisTemplate.opsForValue().set(FILE_CACHE_KEY + fileName, fileUrl, 1, TimeUnit.DAYS);
        } catch (Exception e) {
            logger.warn("Failed to cache file url: {}", fileName, e);
        }
    }
}
