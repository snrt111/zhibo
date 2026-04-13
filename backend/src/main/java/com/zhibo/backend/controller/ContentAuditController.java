package com.zhibo.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.ContentAudit;
import com.zhibo.backend.entity.SensitiveWord;
import com.zhibo.backend.service.ContentAuditService;
import com.zhibo.backend.service.SensitiveWordService;
import com.zhibo.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/audit")
public class ContentAuditController {

    private static final Logger logger = LoggerFactory.getLogger(ContentAuditController.class);

    @Autowired
    private ContentAuditService contentAuditService;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/pending")
    public Result<Page<ContentAudit>> getPendingList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }
        Page<ContentAudit> result = contentAuditService.getPendingList(page, size);
        return Result.success("获取待审核列表成功", result);
    }

    @GetMapping("/list")
    public Result<Page<ContentAudit>> getAuditList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer contentType,
            @RequestParam(required = false) Integer riskLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }
        Page<ContentAudit> result = contentAuditService.getAuditList(page, size, status, contentType, riskLevel);
        return Result.success("获取审核列表成功", result);
    }

    @PostMapping("/audit/{auditId}")
    public Result<Void> manualAudit(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long auditId,
            @RequestBody Map<String, Object> request) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }

        Long auditorId = jwtUtil.getUserIdFromToken(token);
        Integer status = (Integer) request.get("status");
        String auditResult = (String) request.get("auditResult");

        contentAuditService.manualAudit(auditId, auditorId, status, auditResult);
        logger.info("人工审核 {}，状态：{}", auditId, status);
        return Result.success("审核成功", null);
    }

    @PostMapping("/check")
    public Result<Map<String, Object>> checkContent(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        boolean hasSensitive = sensitiveWordService.containsSensitiveWord(content);
        Set<String> foundWords = sensitiveWordService.findSensitiveWords(content);
        String filteredContent = sensitiveWordService.filterSensitiveWords(content);

        Map<String, Object> result = Map.of(
                "hasSensitive", hasSensitive,
                "sensitiveWords", foundWords,
                "filteredContent", filteredContent
        );
        return Result.success("检测完成", result);
    }

    @GetMapping("/sensitive-words")
    public Result<Page<SensitiveWord>> getSensitiveWordList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }
        Page<SensitiveWord> result = sensitiveWordService.getSensitiveWordList(page, size, category, enabled);
        return Result.success("获取敏感词列表成功", result);
    }

    @PostMapping("/sensitive-words")
    public Result<SensitiveWord> addSensitiveWord(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> request) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }

        String word = (String) request.get("word");
        Integer category = request.get("category") != null ? (Integer) request.get("category") : 0;
        Integer level = request.get("level") != null ? (Integer) request.get("level") : 1;

        sensitiveWordService.addSensitiveWord(word, category, level);
        logger.info("添加敏感词：{}", word);
        return Result.success("添加成功", null);
    }

    @DeleteMapping("/sensitive-words/{id}")
    public Result<Void> deleteSensitiveWord(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }

        sensitiveWordService.removeSensitiveWord(id);
        logger.info("删除敏感词：{}", id);
        return Result.success("删除成功", null);
    }

    @PutMapping("/sensitive-words/{id}/toggle")
    public Result<Void> toggleSensitiveWord(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }

        Integer enabled = request.get("enabled");
        sensitiveWordService.toggleSensitiveWord(id, enabled);
        logger.info("切换敏感词状态：{}，状态：{}", id, enabled);
        return Result.success("操作成功", null);
    }

    @PutMapping("/sensitive-words/{id}")
    public Result<Void> updateSensitiveWord(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }

        String word = (String) request.get("word");
        Integer category = request.get("category") != null ? (Integer) request.get("category") : null;
        Integer level = request.get("level") != null ? (Integer) request.get("level") : null;

        sensitiveWordService.updateSensitiveWord(id, word, category, level);
        logger.info("更新敏感词：{}", id);
        return Result.success("更新成功", null);
    }

    @PostMapping("/sensitive-words/reload")
    public Result<Void> reloadSensitiveWords(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }

        sensitiveWordService.reloadSensitiveWords();
        logger.info("重新加载敏感词库");
        return Result.success("重新加载成功", null);
    }
}