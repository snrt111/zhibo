package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhibo.backend.config.AiModelManager;
import com.zhibo.backend.entity.AiModelConfig;
import com.zhibo.backend.exception.BusinessException;
import com.zhibo.backend.mapper.AiModelConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiModelConfigService {
    
    @Autowired
    private AiModelConfigMapper aiModelConfigMapper;
    
    @Autowired
    private AiModelManager aiModelManager;
    
    public static final String MODEL_TYPE_IMAGE = "image";
    public static final String MODEL_TYPE_TEXT = "text";
    public static final String MODEL_TYPE_OLLAMA = "ollama";
    public static final String MODEL_TYPE_THIRDPARTY = "thirdparty";
    
    public List<AiModelConfig> getActiveModels() {
        return aiModelConfigMapper.selectList(
            new LambdaQueryWrapper<AiModelConfig>()
                .eq(AiModelConfig::getStatus, 1)
                .orderByAsc(AiModelConfig::getPriority)
        );
    }
    
    public List<AiModelConfig> getActiveModelsByType(String type) {
        return aiModelConfigMapper.selectList(
            new LambdaQueryWrapper<AiModelConfig>()
                .eq(AiModelConfig::getStatus, 1)
                .eq(AiModelConfig::getType, type)
                .orderByAsc(AiModelConfig::getPriority)
        );
    }
    
    public AiModelConfig getById(Long id) {
        return aiModelConfigMapper.selectById(id);
    }
    
    public AiModelConfig getDefaultModel() {
        List<AiModelConfig> models = getActiveModels();
        return models.isEmpty() ? null : models.get(0);
    }
    
    public Page<AiModelConfig> getAllModels(int page, int size) {
        return aiModelConfigMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<AiModelConfig>()
                .orderByAsc(AiModelConfig::getPriority)
        );
    }
    
    public void create(AiModelConfig config) {
        validateConfig(config);
        aiModelConfigMapper.insert(config);
    }
    
    public void update(AiModelConfig config) {
        if (config.getId() == null) {
            throw new BusinessException("配置ID不能为空");
        }
        validateConfig(config);
        aiModelConfigMapper.updateById(config);
        aiModelManager.refreshModel(config.getId());
    }
    
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException("配置ID不能为空");
        }
        aiModelConfigMapper.deleteById(id);
        aiModelManager.refreshModel(id);
    }
    
    public void updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException("配置ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值无效");
        }
        AiModelConfig config = new AiModelConfig();
        config.setId(id);
        config.setStatus(status);
        aiModelConfigMapper.updateById(config);
        aiModelManager.refreshModel(id);
    }
    
    private void validateConfig(AiModelConfig config) {
        if (config.getName() == null || config.getName().trim().isEmpty()) {
            throw new BusinessException("模型名称不能为空");
        }
        if (config.getType() == null || config.getType().trim().isEmpty()) {
            throw new BusinessException("模型类型不能为空");
        }
        if (config.getBaseUrl() == null || config.getBaseUrl().trim().isEmpty()) {
            throw new BusinessException("API地址不能为空");
        }
        if (config.getModelName() == null || config.getModelName().trim().isEmpty()) {
            throw new BusinessException("模型标识不能为空");
        }
    }
}
