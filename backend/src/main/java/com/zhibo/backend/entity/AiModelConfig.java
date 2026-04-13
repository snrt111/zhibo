package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_model_config")
public class AiModelConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String type;
    
    private String baseUrl;
    
    private String apiKey;
    
    private String modelName;
    
    private Integer status;
    
    private Integer priority;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
