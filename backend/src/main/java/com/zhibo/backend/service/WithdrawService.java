package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.UserBalance;
import com.zhibo.backend.entity.Withdraw;
import com.zhibo.backend.mapper.UserBalanceMapper;
import com.zhibo.backend.mapper.WithdrawMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WithdrawService extends ServiceImpl<WithdrawMapper, Withdraw> {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawService.class);

    @Autowired
    private UserBalanceMapper userBalanceMapper;

    @Transactional
    public Withdraw applyWithdraw(Long userId, BigDecimal amount, String bankCard, String bankName, String accountName) {
        UserBalance balance = getUserBalance(userId);
        if (balance == null) {
            throw new RuntimeException("用户余额账户不存在");
        }

        if (balance.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("可用余额不足");
        }

        if (amount.compareTo(new BigDecimal(10)) < 0) {
            throw new RuntimeException("最低提现金额为10元");
        }

        Withdraw withdraw = new Withdraw();
        withdraw.setUserId(userId);
        withdraw.setAmount(amount);
        withdraw.setBalanceBefore(balance.getAvailableBalance());
        withdraw.setBalanceAfter(balance.getAvailableBalance().subtract(amount));
        withdraw.setStatus(0);
        withdraw.setBankCard(bankCard);
        withdraw.setBankName(bankName);
        withdraw.setAccountName(accountName);
        withdraw.setCreatedAt(LocalDateTime.now());
        withdraw.setUpdatedAt(LocalDateTime.now());

        save(withdraw);

        balance.setAvailableBalance(balance.getAvailableBalance().subtract(amount));
        balance.setFrozenBalance(balance.getFrozenBalance().add(amount));
        userBalanceMapper.updateById(balance);

        logger.info("用户 {} 申请提现 {} 元", userId, amount);
        return withdraw;
    }

    @Transactional
    public boolean auditWithdraw(Long withdrawId, Long auditUserId, Integer status, String remark) {
        Withdraw withdraw = getById(withdrawId);
        if (withdraw == null) {
            throw new RuntimeException("提现申请不存在");
        }

        if (withdraw.getStatus() != 0) {
            throw new RuntimeException("该申请已审核");
        }

        withdraw.setStatus(status);
        withdraw.setAuditUserId(auditUserId);
        withdraw.setAuditTime(LocalDateTime.now());
        withdraw.setAuditRemark(remark);
        withdraw.setUpdatedAt(LocalDateTime.now());

        UserBalance balance = getUserBalance(withdraw.getUserId());

        if (status == 1) {
            balance.setFrozenBalance(balance.getFrozenBalance().subtract(withdraw.getAmount()));
        } else if (status == 2) {
            balance.setFrozenBalance(balance.getFrozenBalance().subtract(withdraw.getAmount()));
            balance.setAvailableBalance(balance.getAvailableBalance().add(withdraw.getAmount()));
        }

        userBalanceMapper.updateById(balance);
        updateById(withdraw);

        logger.info("审核提现申请 {}，状态：{}", withdrawId, status);
        return true;
    }

    @Transactional
    public boolean confirmPay(Long withdrawId) {
        Withdraw withdraw = getById(withdrawId);
        if (withdraw == null) {
            throw new RuntimeException("提现申请不存在");
        }

        if (withdraw.getStatus() != 1) {
            throw new RuntimeException("该申请未通过审核");
        }

        withdraw.setStatus(3);
        withdraw.setPayTime(LocalDateTime.now());
        withdraw.setUpdatedAt(LocalDateTime.now());

        UserBalance balance = getUserBalance(withdraw.getUserId());
        balance.setTotalWithdraw(balance.getTotalWithdraw().add(withdraw.getAmount()));
        userBalanceMapper.updateById(balance);

        updateById(withdraw);
        logger.info("确认打款 {}", withdrawId);
        return true;
    }

    public UserBalance getUserBalance(Long userId) {
        LambdaQueryWrapper<UserBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBalance::getUserId, userId);
        return userBalanceMapper.selectOne(wrapper);
    }

    public UserBalance getOrCreateUserBalance(Long userId) {
        UserBalance balance = getUserBalance(userId);
        if (balance == null) {
            balance = new UserBalance();
            balance.setUserId(userId);
            balance.setTotalIncome(BigDecimal.ZERO);
            balance.setAvailableBalance(BigDecimal.ZERO);
            balance.setFrozenBalance(BigDecimal.ZERO);
            balance.setTotalWithdraw(BigDecimal.ZERO);
            balance.setCreatedAt(LocalDateTime.now());
            balance.setUpdatedAt(LocalDateTime.now());
            userBalanceMapper.insert(balance);
        }
        return balance;
    }

    @Transactional
    public void addIncome(Long userId, BigDecimal amount) {
        UserBalance balance = getOrCreateUserBalance(userId);
        balance.setTotalIncome(balance.getTotalIncome().add(amount));
        balance.setAvailableBalance(balance.getAvailableBalance().add(amount));
        balance.setUpdatedAt(LocalDateTime.now());
        userBalanceMapper.updateById(balance);
    }

    public Page<Withdraw> getWithdrawList(Long userId, int page, int size) {
        Page<Withdraw> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Withdraw> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Withdraw::getUserId, userId);
        wrapper.orderByDesc(Withdraw::getCreatedAt);
        return page(pageObj, wrapper);
    }

    public Page<Withdraw> getPendingAuditList(int page, int size) {
        Page<Withdraw> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Withdraw> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Withdraw::getStatus, 0);
        wrapper.orderByAsc(Withdraw::getCreatedAt);
        return page(pageObj, wrapper);
    }

    public Page<Withdraw> getAllWithdrawList(int page, int size, Integer status) {
        Page<Withdraw> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Withdraw> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Withdraw::getStatus, status);
        }
        wrapper.orderByDesc(Withdraw::getCreatedAt);
        return page(pageObj, wrapper);
    }
}