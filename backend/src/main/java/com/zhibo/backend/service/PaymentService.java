package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.Payment;
import com.zhibo.backend.entity.PaymentConfig;
import com.zhibo.backend.exception.BusinessException;
import com.zhibo.backend.mapper.PaymentMapper;
import com.zhibo.backend.mapper.PaymentConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService extends ServiceImpl<PaymentMapper, Payment> {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PaymentConfigMapper paymentConfigMapper;

    @Autowired
    private WithdrawService withdrawService;

    @Transactional
    public Payment createPayment(Long userId, BigDecimal amount, String paymentMethod, String orderType) {
        if (amount.compareTo(BigDecimal.valueOf(1)) < 0) {
            throw new BusinessException(400, "支付金额不能小于1元");
        }

        String orderId = generateOrderId();

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(0); // 0-待支付
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        paymentMapper.insert(payment);

        logger.info("用户 {} 创建支付订单，订单号 {}，金额 {} 元，支付方式 {}", userId, orderId, amount, paymentMethod);
        return payment;
    }

    @Transactional
    public boolean handlePaymentCallback(String paymentMethod, Map<String, Object> callbackData) {
        String transactionId = (String) callbackData.get("transactionId");
        String orderId = (String) callbackData.get("orderId");
        Integer status = (Integer) callbackData.get("status");

        Payment payment = getPaymentByOrderId(orderId);
        if (payment == null) {
            throw new BusinessException(404, "支付订单不存在");
        }

        if (payment.getPaymentStatus() != 0) {
            throw new BusinessException(400, "该订单已处理");
        }

        payment.setTransactionId(transactionId);
        payment.setPaymentStatus(status);
        payment.setCallbackData(callbackData.toString());
        payment.setUpdatedAt(LocalDateTime.now());

        if (status == 1) { // 支付成功
            payment.setPayTime(LocalDateTime.now());
            // 根据订单类型处理后续逻辑
            handlePaymentSuccess(payment);
        }

        paymentMapper.updateById(payment);

        logger.info("处理支付回调，订单号 {}，支付方式 {}，状态 {}", orderId, paymentMethod, status);
        return true;
    }

    private void handlePaymentSuccess(Payment payment) {
        // 这里可以根据订单类型处理不同的业务逻辑
        // 例如：充值、购买商品等
        if ("recharge".equals(payment.getOrderId().substring(0, 8))) {
            // 充值业务逻辑
            withdrawService.getOrCreateUserBalance(payment.getUserId());
            // 这里应该调用余额服务增加用户余额
        }
    }

    public Payment getPaymentByOrderId(String orderId) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderId, orderId);
        return paymentMapper.selectOne(wrapper);
    }

    public Payment getPaymentByTransactionId(String transactionId) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getTransactionId, transactionId);
        return paymentMapper.selectOne(wrapper);
    }

    public Page<Payment> getPaymentList(Long userId, int page, int size) {
        Page<Payment> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getUserId, userId);
        wrapper.orderByDesc(Payment::getCreatedAt);
        return page(pageObj, wrapper);
    }

    public Page<Payment> getAllPaymentList(int page, int size, String paymentMethod, Integer status) {
        Page<Payment> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        if (paymentMethod != null) {
            wrapper.eq(Payment::getPaymentMethod, paymentMethod);
        }
        if (status != null) {
            wrapper.eq(Payment::getPaymentStatus, status);
        }
        wrapper.orderByDesc(Payment::getCreatedAt);
        return page(pageObj, wrapper);
    }

    public PaymentConfig getPaymentConfig(String paymentMethod, String configKey) {
        LambdaQueryWrapper<PaymentConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentConfig::getPaymentMethod, paymentMethod);
        wrapper.eq(PaymentConfig::getConfigKey, configKey);
        wrapper.eq(PaymentConfig::getStatus, 1);
        return paymentConfigMapper.selectOne(wrapper);
    }

    private String generateOrderId() {
        String prefix = "ORD" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + suffix;
    }
}
