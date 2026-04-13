package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.ContentAudit;
import com.zhibo.backend.mapper.ContentAuditMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ContentAuditService extends ServiceImpl<ContentAuditMapper, ContentAudit> {

    private static final Logger logger = LoggerFactory.getLogger(ContentAuditService.class);

    public static class AuditResult {
        public int status;
        public int riskLevel;
        public String result;
        public boolean shouldBlock;
        public boolean shouldFilter;

        public AuditResult(int status, int riskLevel, String result, boolean shouldBlock, boolean shouldFilter) {
            this.status = status;
            this.riskLevel = riskLevel;
            this.result = result;
            this.shouldBlock = shouldBlock;
            this.shouldFilter = shouldFilter;
        }
    }

    @Autowired
    private SensitiveWordService sensitiveWordService;

    public ContentAudit createAuditRecord(Integer contentType, Long contentId, String content) {
        ContentAudit audit = new ContentAudit();
        audit.setContentType(contentType);
        audit.setContentId(contentId);
        audit.setContent(content);
        audit.setStatus(0);
        audit.setAutoAudit(0);
        audit.setRiskLevel(0);
        audit.setCreatedAt(LocalDateTime.now());
        audit.setUpdatedAt(LocalDateTime.now());

        save(audit);
        return audit;
    }

    public AuditResult checkContent(String content) {
        int riskLevel = sensitiveWordService.getMaxRiskLevel(content);

        AuditResult result;
        if (riskLevel == 0) {
            result = new AuditResult(1, 0, "通过", false, false);
        } else if (riskLevel == 1) {
            result = new AuditResult(1, 1, "低风险，过滤后发布", false, true);
        } else if (riskLevel == 2) {
            result = new AuditResult(0, 2, "中风险，待审核", false, true);
        } else {
            result = new AuditResult(2, 3, "高风险，已拦截", true, false);
        }

        logger.info("内容检测，风险等级：{}，结果：{}", riskLevel, result.result);
        return result;
    }

    public void saveAuditRecord(Integer contentType, Long contentId, String content, AuditResult auditResult) {
        ContentAudit audit = new ContentAudit();
        audit.setContentType(contentType);
        audit.setContentId(contentId);
        audit.setContent(content);
        audit.setAutoAudit(1);
        audit.setAuditTime(LocalDateTime.now());
        audit.setCreatedAt(LocalDateTime.now());
        audit.setUpdatedAt(LocalDateTime.now());
        audit.setRiskLevel(auditResult.riskLevel);
        audit.setStatus(auditResult.status);
        audit.setAuditResult("自动审核：" + auditResult.result);

        save(audit);
        logger.info("保存审核记录，类型：{}，ID：{}，风险等级：{}，结果：{}", contentType, contentId, auditResult.riskLevel, auditResult.result);
    }

    public AuditResult autoAudit(Integer contentType, Long contentId, String content) {
        AuditResult result = checkContent(content);
        saveAuditRecord(contentType, contentId, content, result);
        return result;
    }

    public boolean manualAudit(Long auditId, Long auditorId, Integer status, String auditResult) {
        ContentAudit audit = getById(auditId);
        if (audit == null) {
            throw new RuntimeException("审核记录不存在");
        }

        audit.setStatus(status);
        audit.setAuditorId(auditorId);
        audit.setAuditTime(LocalDateTime.now());
        audit.setAuditResult(auditResult);
        audit.setUpdatedAt(LocalDateTime.now());

        updateById(audit);
        logger.info("人工审核 {}，状态：{}", auditId, status);
        return true;
    }

    public Page<ContentAudit> getPendingList(int page, int size) {
        Page<ContentAudit> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<ContentAudit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContentAudit::getStatus, 0);
        wrapper.orderByAsc(ContentAudit::getCreatedAt);
        return page(pageObj, wrapper);
    }

    public Page<ContentAudit> getAuditList(int page, int size, Integer status, Integer contentType, Integer riskLevel) {
        Page<ContentAudit> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<ContentAudit> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ContentAudit::getStatus, status);
        }
        if (contentType != null) {
            wrapper.eq(ContentAudit::getContentType, contentType);
        }
        if (riskLevel != null) {
            wrapper.eq(ContentAudit::getRiskLevel, riskLevel);
        }
        wrapper.orderByDesc(ContentAudit::getCreatedAt);
        return page(pageObj, wrapper);
    }
}