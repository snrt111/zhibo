package com.zhibo.backend.controller;

import com.zhibo.backend.common.Result;
import com.zhibo.backend.service.StorageService;
import com.zhibo.backend.util.FileValidator;
import com.zhibo.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传控制器
 * 提供图片、头像等文件的上传和删除功能
 *
 * @author zhibo
 * @since 2026-02-01
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private StorageService storageService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 上传图片
     * 支持 JPG、PNG、GIF、WebP 格式
     *
     * @param file 图片文件
     * @return 图片访问URL
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        logger.info("Uploading image, file name: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());
        FileValidator.validateImage(file);
        String fileUrl = storageService.uploadFile(file, "images");
        logger.info("Image uploaded successfully, url: {}", fileUrl);
        return Result.success("上传成功", Map.of("url", fileUrl));
    }

    /**
     * 上传用户头像
     * 需要用户登录状态，从Token中获取用户ID
     * 头像会关联到用户账户
     *
     * @param authorization 授权Token
     * @param file 头像文件
     * @return 头像访问URL
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("file") MultipartFile file) {
        logger.info("Uploading avatar, file name: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());
        FileValidator.validateAvatar(file);
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.debug("Uploading avatar for user: {}", userId);
        String fileUrl = storageService.uploadAvatar(file, userId);
        logger.info("Avatar uploaded successfully, url: {}", fileUrl);
        return Result.success("上传成功", Map.of("url", fileUrl));
    }

    /**
     * 删除文件
     * 根据文件URL删除存储的文件
     *
     * @param request 包含文件url的请求体
     * @return 删除结果
     */
    @DeleteMapping("/file")
    public Result<String> deleteFile(@RequestBody Map<String, String> request) {
        String fileUrl = request.get("url");
        logger.info("Deleting file: {}", fileUrl);
        storageService.deleteFile(fileUrl);
        logger.info("File deleted successfully: {}", fileUrl);
        return Result.success("删除成功", null);
    }
}
