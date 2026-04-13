package com.zhibo.backend.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 * 提供文件上传、下载、删除等存储操作
 * 支持本地存储和MinIO分布式存储两种实现
 *
 * @author zhibo
 * @since 2026-02-01
 */
public interface StorageService {

    /**
     * 上传文件到指定目录
     *
     * @param file 上传的文件
     * @param directory 目标目录
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String directory);

    /**
     * 上传用户头像
     * 头像会关联到用户ID，并使用特定的命名规则
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像访问URL
     */
    String uploadAvatar(MultipartFile file, Long userId);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);

    /**
     * 获取文件访问URL
     *
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    String getFileUrl(String objectName);

    /**
     * 检查文件是否存在
     *
     * @param fileUrl 文件URL
     * @return 文件是否存在
     */
    boolean exists(String fileUrl);
}
