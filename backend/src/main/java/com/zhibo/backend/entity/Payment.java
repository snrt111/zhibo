package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderId;

    private Long userId;

    private BigDecimal amount;

    private String paymentMethod;

    private Integer paymentStatus;

    private String transactionId;

    private String callbackData;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime payTime;
}
