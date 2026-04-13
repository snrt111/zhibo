package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("generated_image")
public class GeneratedImage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String prompt;
    
    private String imageUrl;
    
    private Long modelId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
