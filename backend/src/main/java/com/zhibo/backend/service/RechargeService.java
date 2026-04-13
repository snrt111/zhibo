package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.Recharge;
import com.zhibo.backend.entity.UserBalance;
import com.zhibo.backend.exception.BusinessException;
import com.zhibo.backend.mapper.RechargeMapper;
import com.zhibo.backend.mapper.UserBalanceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RechargeService extends ServiceImpl<RechargeMapper, Recharge> {

    private static final Logger logger = LoggerFactory.getLogger(RechargeService.class);

    @Autowired
    private RechargeMapper rechargeMapper;

    @Autowired
    private UserBalanceMapper userBalanceMapper;

    @Autowired
    private WithdrawService withdrawService;

    @Transactional
    public Recharge createRecharge(Long userId, BigDecimal amount, String paymentMethod) {
        if (amount.compareTo(BigDecimal.valueOf(1)) < 0) {
            throw new BusinessException(400, "充值金额不能小于1元");
        }

        Recharge recharge = new Recharge();
        recharge.setUserId(userId);
        recharge.setAmount(amount);
        recharge.setPaymentMethod(paymentMethod);
        recharge.setTransactionId(UUID.randomUUID().toString());
        recharge.setStatus(0);
        recharge.setCreatedAt(LocalDateTime.now());
        recharge.setUpdatedAt(LocalDateTime.now());

        rechargeMapper.insert(recharge);

        logger.info("用户 {} 创建充值订单，金额 {} 元，支付方式 {}", userId, amount, paymentMethod);
        return recharge;
    }

    @Transactional
    public boolean completeRecharge(Long rechargeId) {
        Recharge recharge = getById(rechargeId);
        if (recharge == null) {
            throw new BusinessException(404, "充值订单不存在");
        }

        if (recharge.getStatus() != 0) {
            throw new BusinessException(400, "该订单已处理");
        }

        recharge.setStatus(1);
        recharge.setPayTime(LocalDateTime.now());
        recharge.setUpdatedAt(LocalDateTime.now());
        rechargeMapper.updateById(recharge);

        UserBalance balance = withdrawService.getOrCreateUserBalance(recharge.getUserId());
        balance.setAvailableBalance(balance.getAvailableBalance().add(recharge.getAmount()));
        balance.setUpdatedAt(LocalDateTime.now());
        userBalanceMapper.updateById(balance);

        logger.info("用户 {} 充值成功，金额 {} 元", recharge.getUserId(), recharge.getAmount());
        return true;
    }

    public Page<Recharge> getRechargeList(Long userId, int page, int size) {
        Page<Recharge> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Recharge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recharge::getUserId, userId);
        wrapper.orderByDesc(Recharge::getCreatedAt);
        return page(pageObj, wrapper);
    }

    public Page<Recharge> getAllRechargeList(int page, int size, Integer status) {
        Page<Recharge> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Recharge> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Recharge::getStatus, status);
        }
        wrapper.orderByDesc(Recharge::getCreatedAt);
        return page(pageObj, wrapper);
    }
}
