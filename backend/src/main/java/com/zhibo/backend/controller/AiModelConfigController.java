package com.zhibo.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.AiModelConfig;
import com.zhibo.backend.service.AiModelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/ai-config")
public class AiModelConfigController {
    
    @Autowired
    private AiModelConfigService aiModelConfigService;
    
    @GetMapping("/list")
    public Result<Page<AiModelConfig>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AiModelConfig> result = aiModelConfigService.getAllModels(page, size);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    public Result<AiModelConfig> getById(@PathVariable Long id) {
        AiModelConfig config = aiModelConfigService.getById(id);
        return Result.success(config);
    }
    
    @PostMapping
    public Result<Void> create(@RequestBody AiModelConfig config) {
        aiModelConfigService.create(config);
        return Result.success();
    }
    
    @PutMapping
    public Result<Void> update(@RequestBody AiModelConfig config) {
        aiModelConfigService.update(config);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        aiModelConfigService.delete(id);
        return Result.success();
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        aiModelConfigService.updateStatus(id, status);
        return Result.success();
    }
}
