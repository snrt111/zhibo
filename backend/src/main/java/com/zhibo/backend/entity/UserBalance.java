package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_balance")
public class UserBalance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal totalIncome;

    private BigDecimal availableBalance;

    private BigDecimal frozenBalance;

    private BigDecimal totalWithdraw;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}