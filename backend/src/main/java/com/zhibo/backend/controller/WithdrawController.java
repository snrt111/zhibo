package com.zhibo.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.UserBalance;
import com.zhibo.backend.entity.Withdraw;
import com.zhibo.backend.service.WithdrawService;
import com.zhibo.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/withdraw")
public class WithdrawController {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawController.class);

    @Autowired
    private WithdrawService withdrawService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/balance")
    public Result<UserBalance> getBalance(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        UserBalance balance = withdrawService.getOrCreateUserBalance(userId);
        return Result.success("获取余额成功", balance);
    }

    @PostMapping("/apply")
    public Result<Withdraw> applyWithdraw(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, Object> request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String bankCard = (String) request.get("bankCard");
        String bankName = (String) request.get("bankName");
        String accountName = (String) request.get("accountName");

        Withdraw withdraw = withdrawService.applyWithdraw(userId, amount, bankCard, bankName, accountName);
        logger.info("用户 {} 申请提现 {} 元", userId, amount);
        return Result.success("提现申请成功", withdraw);
    }

    @GetMapping("/my-list")
    public Result<Page<Withdraw>> getMyWithdrawList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Page<Withdraw> result = withdrawService.getWithdrawList(userId, page, size);
        return Result.success("获取提现记录成功", result);
    }

    @GetMapping("/pending")
    public Result<Page<Withdraw>> getPendingList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }
        Page<Withdraw> result = withdrawService.getPendingAuditList(page, size);
        return Result.success("获取待审核列表成功", result);
    }

    @GetMapping("/list")
    public Result<Page<Withdraw>> getWithdrawList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }
        Page<Withdraw> result = withdrawService.getAllWithdrawList(page, size, status);
        return Result.success("获取提现列表成功", result);
    }

    @PostMapping("/audit/{withdrawId}")
    public Result<Void> auditWithdraw(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long withdrawId,
            @RequestBody Map<String, Object> request) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }

        Long auditUserId = jwtUtil.getUserIdFromToken(token);
        Integer status = (Integer) request.get("status");
        String remark = (String) request.get("remark");

        withdrawService.auditWithdraw(withdrawId, auditUserId, status, remark);
        logger.info("审核提现申请 {}，状态：{}", withdrawId, status);
        return Result.success("审核成功", null);
    }

    @PostMapping("/pay/{withdrawId}")
    public Result<Void> confirmPay(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long withdrawId) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }

        withdrawService.confirmPay(withdrawId);
        logger.info("确认打款 {}", withdrawId);
        return Result.success("打款成功", null);
    }

    @GetMapping("/{withdrawId}")
    public Result<Withdraw> getWithdrawDetail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long withdrawId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer userType = jwtUtil.getUserTypeFromToken(token);

        Withdraw withdraw = withdrawService.getById(withdrawId);
        if (withdraw == null) {
            return Result.error(404, "提现申请不存在");
        }

        if (userType != 1 && !withdraw.getUserId().equals(userId)) {
            return Result.error(403, "无权访问");
        }

        return Result.success("获取成功", withdraw);
    }
}