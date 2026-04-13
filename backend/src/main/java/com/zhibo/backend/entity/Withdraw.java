package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("withdraw")
public class Withdraw {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal amount;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private Integer status;

    private String bankCard;

    private String bankName;

    private String accountName;

    private Long auditUserId;

    private LocalDateTime auditTime;

    private String auditRemark;

    private LocalDateTime payTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}