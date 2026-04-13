package com.zhibo.backend.controller;

import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.Payment;
import com.zhibo.backend.service.PaymentService;
import com.zhibo.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public Result<Payment> createPayment(
            @RequestHeader("Authorization") String authorization,
            @RequestBody PaymentRequest request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Payment payment = paymentService.createPayment(userId, request.getAmount(), request.getPaymentMethod(), request.getOrderType());
        return Result.success("创建支付订单成功", payment);
    }

    @PostMapping("/callback/{method}")
    public Result<Void> handlePaymentCallback(
            @PathVariable String method,
            @RequestBody Map<String, Object> callbackData) {
        paymentService.handlePaymentCallback(method, callbackData);
        return Result.success();
    }

    @GetMapping("/query/{orderId}")
    public Result<Payment> queryPayment(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String orderId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        if (payment == null) {
            return Result.error(404, "支付订单不存在");
        }
        
        if (!payment.getUserId().equals(userId)) {
            return Result.error(403, "无权查看该订单");
        }
        
        return Result.success("查询成功", payment);
    }

    @GetMapping("/list")
    public Result<?> getPaymentList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        return Result.success("获取支付记录成功", paymentService.getPaymentList(userId, page - 1, size));
    }

    @GetMapping("/admin/list")
    public Result<?> getAllPaymentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) Integer status) {
        return Result.success("获取支付记录成功", paymentService.getAllPaymentList(page - 1, size, paymentMethod, status));
    }

    static class PaymentRequest {
        private BigDecimal amount;
        private String paymentMethod;
        private String orderType;

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }
    }
}
