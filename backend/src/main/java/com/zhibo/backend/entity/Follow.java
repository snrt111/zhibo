package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("follow")
public class Follow {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    @TableField("follow_user_id")
    private Long followedUserId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
