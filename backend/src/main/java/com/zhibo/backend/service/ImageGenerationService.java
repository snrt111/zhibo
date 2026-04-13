package com.zhibo.backend.service;

import com.zhibo.backend.config.AiModelManager;
import com.zhibo.backend.config.MinioConfig;
import com.zhibo.backend.entity.AiModelConfig;
import com.zhibo.backend.entity.GeneratedImage;
import com.zhibo.backend.exception.BusinessException;
import com.zhibo.backend.mapper.GeneratedImageMapper;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;

@Service
public class ImageGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationService.class);
    
    @Autowired
    private AiModelConfigService aiModelConfigService;
    
    @Autowired
    private GeneratedImageMapper generatedImageMapper;
    
    @Autowired
    private MinioClient minioClient;
    
    @Autowired
    private MinioConfig minioConfig;
    
    @Autowired
    private AiModelManager aiModelManager;
    
    public GeneratedImage generateImage(String prompt, Long modelId, Long userId) {
        AiModelConfig modelConfig = modelId != null 
            ? aiModelConfigService.getById(modelId)
            : aiModelConfigService.getDefaultModel();
        
        if (modelConfig == null) {
            throw new BusinessException("未找到可用的AI模型配置");
        }
        
        try {
            logger.info("Generating image with model: {}, prompt: {}", modelConfig.getName(), prompt);
            
            ImageModel imageModel = aiModelManager.getImageModel(modelConfig.getId());
            
            OpenAiImageOptions options = OpenAiImageOptions.builder()
                .withModel(modelConfig.getModelName() != null ? modelConfig.getModelName() : "dall-e-3")
                .withHeight(1024)
                .withWidth(1024)
                .withResponseFormat("b64_json")
                .build();
            
            ImagePrompt imagePrompt = new ImagePrompt(prompt, options);
            ImageResponse response = imageModel.call(imagePrompt);
            
            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                throw new BusinessException("图像生成API返回空结果");
            }
            
            Image image = response.getResults().get(0).getOutput();
            String b64Json = image.getB64Json();
            
            if (b64Json == null || b64Json.isEmpty()) {
                throw new BusinessException("图像数据为空");
            }
            
            byte[] imageData = Base64.getDecoder().decode(b64Json);
            
            if (!isValidImageData(imageData)) {
                throw new BusinessException("生成的图像数据无效");
            }
            
            String fileName = "generated-images/" + UUID.randomUUID() + ".png";
            String imageUrl = uploadToMinio(imageData, fileName);
            
            GeneratedImage record = new GeneratedImage();
            record.setUserId(userId);
            record.setPrompt(prompt);
            record.setImageUrl(imageUrl);
            record.setModelId(modelConfig.getId());
            generatedImageMapper.insert(record);
            
            logger.info("Image generated successfully: {}", imageUrl);
            return record;
        } catch (Exception e) {
            logger.error("Failed to generate image", e);
            throw new BusinessException("图片生成失败: " + e.getMessage());
        }
    }
    
    private boolean isValidImageData(byte[] data) {
        if (data == null || data.length < 8) {
            return false;
        }
        
        if (data[0] == (byte) 0x89 && data[1] == (byte) 0x50 && data[2] == (byte) 0x4E && data[3] == (byte) 0x47) {
            return true;
        }
        
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF) {
            return true;
        }
        
        if (data[0] == (byte) 'G' && data[1] == (byte) 'I' && data[2] == (byte) 'F') {
            return true;
        }
        
        if (data[0] == (byte) 'B' && data[1] == (byte) 'M') {
            return true;
        }
        
        return false;
    }
    
    private String uploadToMinio(byte[] data, String fileName) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(inputStream, data.length, -1)
                    .contentType("image/png")
                    .build()
            );
            return minioConfig.getPublicUrl() + "/" + minioConfig.getBucketName() + "/" + fileName;
        } catch (Exception e) {
            logger.error("Failed to upload to MinIO", e);
            throw new BusinessException("文件上传失败");
        }
    }
}
