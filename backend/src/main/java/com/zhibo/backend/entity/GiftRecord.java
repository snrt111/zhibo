package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gift_record")
public class GiftRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long liveId;
    private Long fromUserId;
    private Long toUserId;
    private Long giftId;
    private Integer count;
    private Double totalAmount;
    private LocalDateTime createdAt;
}