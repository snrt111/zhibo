package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recharge")
public class Recharge {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal amount;

    private String paymentMethod;

    private String transactionId;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime payTime;
}
