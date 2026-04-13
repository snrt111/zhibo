package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content_audit")
public class ContentAudit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer contentType;

    private Long contentId;

    private String content;

    private Integer status;

    private Long auditorId;

    private LocalDateTime auditTime;

    private String auditResult;

    private Integer autoAudit;

    private Integer riskLevel;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}