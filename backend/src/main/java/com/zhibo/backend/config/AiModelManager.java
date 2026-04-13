package com.zhibo.backend.config;

import com.zhibo.backend.entity.AiModelConfig;
import com.zhibo.backend.mapper.AiModelConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiModelManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AiModelManager.class);
    
    @Autowired
    private AiModelConfigMapper aiModelConfigMapper;
    
    private final Map<Long, ImageModel> imageModelCache = new ConcurrentHashMap<>();
    
    public ImageModel getImageModel(Long modelId) {
        return imageModelCache.computeIfAbsent(modelId, this::createImageModel);
    }
    
    public ImageModel getDefaultImageModel() {
        AiModelConfig config = getDefaultModelConfig();
        if (config == null) {
            throw new RuntimeException("未找到默认的AI模型配置");
        }
        return getImageModel(config.getId());
    }
    
    private AiModelConfig getDefaultModelConfig() {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiModelConfig>();
        wrapper.eq(AiModelConfig::getStatus, 1)
               .orderByAsc(AiModelConfig::getPriority)
               .last("LIMIT 1");
        return aiModelConfigMapper.selectOne(wrapper);
    }
    
    private ImageModel createImageModel(Long modelId) {
        AiModelConfig config = aiModelConfigMapper.selectById(modelId);
        if (config == null) {
            throw new RuntimeException("未找到AI模型配置: " + modelId);
        }
        
        logger.info("Creating ImageModel for model: {} (type={}, url={})", 
            config.getName(), config.getType(), config.getBaseUrl());
        
        return createOpenAiModel(config);
    }
    
    private ImageModel createOpenAiModel(AiModelConfig config) {
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new RuntimeException("AI模型配置缺少baseUrl");
        }
        
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        String apiKey = config.getApiKey() != null ? config.getApiKey() : "dummy-key";
        
        OpenAiImageApi openAiImageApi = OpenAiImageApi.builder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .build();
        
        return new OpenAiImageModel(openAiImageApi);
    }
    
    public void refreshModel(Long modelId) {
        imageModelCache.remove(modelId);
        logger.info("Refreshed ImageModel for model: {}", modelId);
    }
    
    public void refreshAllModels() {
        imageModelCache.clear();
        logger.info("Refreshed all ImageModels");
    }
}
