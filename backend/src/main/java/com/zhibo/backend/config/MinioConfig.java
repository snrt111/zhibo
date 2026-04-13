package com.zhibo.backend.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO对象存储配置类
 * 提供文件上传、下载、删除等存储服务的配置
 * 支持bucket自动创建和公开访问策略设置
 *
 * @author zhibo
 * @since 2026-02-01
 */
@Getter
@Configuration
public class MinioConfig {

    private static final Logger logger = LoggerFactory.getLogger(MinioConfig.class);

    /** MinIO服务端点（内部网络地址） */
    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    /** MinIO访问密钥ID */
    @Value("${minio.accessKey:minioadmin}")
    private String accessKey;

    /** MinIO访问密钥密码 */
    @Value("${minio.secretKey:minioadmin123}")
    private String secretKey;

    /** 存储桶名称 */
    @Value("${minio.bucketName:zhibo-files}")
    private String bucketName;

    /** MinIO对外访问URL前缀（用于返回给前端的文件访问地址） */
    @Value("${minio.public-url:http://localhost:9000}")
    private String publicUrl;

    /** 连接超时时间（毫秒） */
    @Value("${minio.connectTimeout:10000}")
    private int connectTimeout;

    /** 写入超时时间（毫秒） */
    @Value("${minio.writeTimeout:60000}")
    private int writeTimeout;

    /** 读取超时时间（毫秒） */
    @Value("${minio.readTimeout:60000}")
    private int readTimeout;

    /**
     * 创建MinIO客户端Bean
     * 初始化客户端连接并自动创建存储桶
     *
     * @return MinIO客户端实例
     */
    @Bean
    public MinioClient minioClient() {
        logger.info("Initializing MinIO client, endpoint: {}, bucket: {}", endpoint, bucketName);
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        initBucket(client);
        logger.info("MinIO client initialized successfully");
        return client;
    }

    /**
     * 初始化存储桶
     * 如果bucket不存在则创建并设置公开读取策略
     *
     * @param client MinIO客户端实例
     */
    private void initBucket(MinioClient client) {
        logger.debug("Checking if bucket exists: {}", bucketName);
        try {
            boolean found = client.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!found) {
                logger.info("Bucket not found, creating new bucket: {}", bucketName);
                client.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
                );
                client.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(createBucketPolicy(bucketName))
                        .build()
                );
                logger.info("MinIO bucket created successfully: {}", bucketName);
            } else {
                logger.info("MinIO bucket already exists: {}", bucketName);
            }
        } catch (Exception e) {
            logger.warn("Failed to initialize MinIO bucket: {}", bucketName, e);
        }
    }

    /**
     * 创建存储桶公开访问策略
     * 允许公开读取bucket中的文件
     *
     * @param bucket 存储桶名称
     * @return JSON格式的策略配置
     */
    private String createBucketPolicy(String bucket) {
        return String.format(
            "{\"Version\":\"2012-10-17\",\"Statement\":[" +
            "{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]}," +
            "\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\"]," +
            "\"Resource\":[\"arn:aws:s3:::%s\"]}," +
            "{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]}," +
            "\"Action\":[\"s3:GetObject\"]," +
            "\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}",
            bucket, bucket
        );
    }
}
