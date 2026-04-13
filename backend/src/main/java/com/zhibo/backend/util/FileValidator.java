package com.zhibo.backend.util;

import com.zhibo.backend.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileValidator {

    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(
        Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp")
    );

    private static final Set<String> IMAGE_CONTENT_TYPES = new HashSet<>(
        Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp")
    );

    private static final long DEFAULT_MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final long DEFAULT_MAX_AVATAR_SIZE = 10 * 1024 * 1024;

    private FileValidator() {
    }

    public static void validateImage(MultipartFile file) {
        validateImage(file, DEFAULT_MAX_IMAGE_SIZE);
    }

    public static void validateAvatar(MultipartFile file) {
        validateImage(file, DEFAULT_MAX_AVATAR_SIZE);
    }

    public static void validateImage(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!IMAGE_EXTENSIONS.contains(suffix.toLowerCase())) {
            throw new BusinessException("只能上传图片文件（JPG/PNG/GIF/WebP）");
        }

        String contentType = file.getContentType();
        if (contentType != null && !IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("文件类型不合法");
        }

        if (file.getSize() > maxSize) {
            throw new BusinessException("图片大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }
    }

    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDot = filename.lastIndexOf(".");
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot).toLowerCase();
    }

    public static boolean isImageFile(String filename) {
        String extension = getFileExtension(filename);
        return IMAGE_EXTENSIONS.contains(extension);
    }

    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
