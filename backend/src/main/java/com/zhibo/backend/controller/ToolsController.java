package com.zhibo.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.AiModelConfig;
import com.zhibo.backend.entity.GeneratedImage;
import com.zhibo.backend.mapper.GeneratedImageMapper;
import com.zhibo.backend.service.AiModelConfigService;
import com.zhibo.backend.service.ImageGenerationService;
import com.zhibo.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tools")
public class ToolsController {
    
    @Autowired
    private AiModelConfigService aiModelConfigService;
    
    @Autowired
    private ImageGenerationService imageGenerationService;
    
    @Autowired
    private GeneratedImageMapper generatedImageMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping("/image/models")
    public Result<List<Map<String, Object>>> getImageModels(
            @RequestParam(value = "type", required = false) String type) {
        List<AiModelConfig> models;
        if (type != null && !type.isEmpty()) {
            models = aiModelConfigService.getActiveModelsByType(type);
        } else {
            models = aiModelConfigService.getActiveModels();
        }
        List<Map<String, Object>> result = models.stream().map(model -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", model.getId());
            map.put("name", model.getName());
            map.put("type", model.getType());
            return map;
        }).toList();
        return Result.success(result);
    }
    
    @PostMapping("/image/generate")
    public Result<GeneratedImage> generateImage(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String prompt = (String) request.get("prompt");
        Long modelId = request.get("modelId") != null ? Long.valueOf(request.get("modelId").toString()) : null;
        
        Long userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            userId = jwtUtil.getUserIdFromToken(token);
        }
        
        GeneratedImage image = imageGenerationService.generateImage(prompt, modelId, userId);
        return Result.success(image);
    }
    
    @GetMapping("/image/history")
    public Result<List<GeneratedImage>> getImageHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Long userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            userId = jwtUtil.getUserIdFromToken(token);
        }
        
        LambdaQueryWrapper<GeneratedImage> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(GeneratedImage::getUserId, userId);
        }
        wrapper.orderByDesc(GeneratedImage::getCreatedAt)
               .last("LIMIT 20");
        
        List<GeneratedImage> images = generatedImageMapper.selectList(wrapper);
        return Result.success(images);
    }
}
