package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reporterId;

    private Integer targetType;

    private Long targetId;

    private String reason;

    private String description;

    private String evidence;

    private Integer status;

    private Long handlerId;

    private LocalDateTime handleTime;

    private String handleResult;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}