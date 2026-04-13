package com.zhibo.backend.service;

import com.zhibo.backend.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(name = "upload.use-minio", havingValue = "false", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageService.class);

    @Value("${upload.path:/app/uploads}")
    private String uploadPath;

    @Value("${upload.url-prefix:http://localhost:8081/api/uploads}")
    private String urlPrefix;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String AVATAR_CACHE_KEY = "avatar:url:";

    @PostConstruct
    public void init() {
        // Use current directory if uploadPath is not writable
        File baseDir = new File(uploadPath);
        if (!baseDir.exists() || !baseDir.canWrite()) {
            // Fallback to current directory
            uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
            baseDir = new File(uploadPath);
            logger.info("Using fallback upload directory: {}", uploadPath);
        }
        
        if (!baseDir.exists()) {
            boolean created = baseDir.mkdirs();
            if (!created) {
                logger.warn("Failed to create base upload directory: {}", uploadPath);
            } else {
                logger.info("Created base upload directory: {}", uploadPath);
            }
        }
        
        // Create subdirectories
        String[] subdirs = {"images", "avatar"};
        for (String subdir : subdirs) {
            File dir = new File(uploadPath, subdir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (created) {
                    logger.info("Created subdirectory: {}", dir.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        String fileName = generateFileName(file);
        String fullPath = uploadPath + File.separator + directory;
        saveFile(file, fullPath, fileName);
        String fileUrl = urlPrefix + "/" + directory + "/" + fileName;
        logger.info("File uploaded: {}", fileUrl);
        return fileUrl;
    }

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        String fileName = generateAvatarName(file, userId);
        saveFile(file, uploadPath, fileName);
        String fileUrl = urlPrefix + "/" + fileName;
        cacheAvatarUrl(userId, fileUrl);
        logger.info("Avatar uploaded: {}", fileName);
        return fileUrl;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        String filePath = extractFilePath(fileUrl);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
            logger.info("File deleted: {}", filePath);
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        return urlPrefix + "/" + objectName;
    }

    @Override
    public boolean exists(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }
        String fileName = extractFileName(fileUrl);
        File file = new File(uploadPath + File.separator + fileName);
        return file.exists();
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

    private String generateFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("文件名不能为空");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID() + suffix;
    }

    private String generateAvatarName(MultipartFile file, Long userId) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("文件名不能为空");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        return "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + suffix;
    }

    private void saveFile(MultipartFile file, String directory, String fileName) {
        try {
            File uploadDir = new File(directory);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    logger.error("Failed to create directory: {}", directory);
                    throw new BusinessException("无法创建上传目录");
                }
            }
            File destFile = new File(uploadDir, fileName);
            logger.info("Saving file to: {}", destFile.getAbsolutePath());
            file.transferTo(destFile);
        } catch (IOException e) {
            logger.error("Failed to save file: {}", fileName, e);
            throw new BusinessException("文件上传失败");
        }
    }

    private String extractFileName(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }

    private String extractFilePath(String fileUrl) {
        String relativePath = fileUrl.substring(urlPrefix.length());
        return uploadPath + relativePath.replace("/", File.separator);
    }
}
