package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gift")
public class Gift {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Double price;
    private String icon;
    private String animation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}