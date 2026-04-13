package com.zhibo.backend.controller;

import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.Recharge;
import com.zhibo.backend.service.RechargeService;
import com.zhibo.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/recharge")
public class RechargeController {

    private static final Logger logger = LoggerFactory.getLogger(RechargeController.class);

    @Autowired
    private RechargeService rechargeService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public Result<Recharge> createRecharge(
            @RequestHeader("Authorization") String authorization,
            @RequestBody RechargeRequest request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Recharge recharge = rechargeService.createRecharge(userId, request.getAmount(), request.getPaymentMethod());
        return Result.success("创建充值订单成功", recharge);
    }

    static class RechargeRequest {
        private BigDecimal amount;
        private String paymentMethod;

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
    }

    @PostMapping("/complete/{id}")
    public Result<Void> completeRecharge(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        rechargeService.completeRecharge(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<?> getRechargeList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        return Result.success("获取充值记录成功", rechargeService.getRechargeList(userId, page - 1, size));
    }

    @GetMapping("/admin/list")
    public Result<?> getAllRechargeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success("获取充值记录成功", rechargeService.getAllRechargeList(page - 1, size, status));
    }
}
