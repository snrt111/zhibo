package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.controller.WebSocketController;
import com.zhibo.backend.entity.Gift;
import com.zhibo.backend.entity.GiftRecord;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.mapper.GiftMapper;
import com.zhibo.backend.mapper.GiftRecordMapper;
import com.zhibo.backend.mapper.UserBalanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.zhibo.backend.entity.UserBalance;
import com.zhibo.backend.exception.BusinessException;

@Service
public class GiftService extends ServiceImpl<GiftMapper, Gift> {

    @Autowired
    private GiftRecordMapper giftRecordMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private UserService userService;

    @Autowired
    private WithdrawService withdrawService;

    @Autowired
    private UserBalanceMapper userBalanceMapper;

    @Autowired
    private StorageService storageService;

    /**
     * 获取所有礼物列表（按价格升序排列）
     */
    public List<Gift> getGiftList() {
        LambdaQueryWrapper<Gift> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Gift::getPrice);
        return list(wrapper);
    }

    /**
     * 获取热门礼物列表
     */
    public List<Gift> getHotGiftList() {
        LambdaQueryWrapper<Gift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Gift::getIsHot, true);
        wrapper.orderByAsc(Gift::getPrice);
        return list(wrapper);
    }

    /**
     * 获取管理员礼物列表（分页）
     */
    public Page<Gift> getAdminGiftList(String keyword, Double minPrice, Double maxPrice, int page, int size) {
        Page<Gift> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Gift> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Gift::getName, keyword);
        }
        if (minPrice != null) {
            wrapper.ge(Gift::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Gift::getPrice, maxPrice);
        }
        
        wrapper.orderByDesc(Gift::getCreatedAt);
        return page(pageParam, wrapper);
    }

    /**
     * 根据ID获取礼物
     */
    public Gift getGiftById(Long id) {
        return getById(id);
    }

    /**
     * 创建礼物（带图片上传或URL）
     */
    public Gift createGift(Gift gift, MultipartFile iconFile, String iconUrl) {
        // 上传礼物图标文件
        if (iconFile != null && !iconFile.isEmpty()) {
            String uploadedIconUrl = storageService.uploadFile(iconFile, "gift");
            gift.setIcon(uploadedIconUrl);
        } else if (iconUrl != null && !iconUrl.isEmpty()) {
            // 使用传入的URL作为图标
            gift.setIcon(iconUrl);
        }
        gift.setCreatedAt(LocalDateTime.now());
        gift.setUpdatedAt(LocalDateTime.now());
        save(gift);
        return gift;
    }

    /**
     * 更新礼物（带图片上传或URL）
     */
    public Gift updateGift(Gift gift, MultipartFile iconFile, String iconUrl) {
        Gift existingGift = getById(gift.getId());
        if (existingGift == null) {
            throw new BusinessException(404, "礼物不存在");
        }
        // 上传新的礼物图标文件
        if (iconFile != null && !iconFile.isEmpty()) {
            // 删除旧图标
            if (existingGift.getIcon() != null && !existingGift.getIcon().isEmpty()) {
                storageService.deleteFile(existingGift.getIcon());
            }
            String newIconUrl = storageService.uploadFile(iconFile, "gift");
            gift.setIcon(newIconUrl);
        } else if (iconUrl != null && !iconUrl.isEmpty()) {
            // 使用传入的URL作为图标
            gift.setIcon(iconUrl);
        } else {
            // 如果没有上传新图标，保留原图标
            gift.setIcon(existingGift.getIcon());
        }
        
        // 使用UpdateWrapper确保所有字段都被更新（包括isHot为false的情况）
        LambdaUpdateWrapper<Gift> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Gift::getId, gift.getId());
        
        if (gift.getName() != null) {
            updateWrapper.set(Gift::getName, gift.getName());
        }
        if (gift.getPrice() != null) {
            updateWrapper.set(Gift::getPrice, gift.getPrice());
        }
        if (gift.getIcon() != null) {
            updateWrapper.set(Gift::getIcon, gift.getIcon());
        }
        // isHot可能为true或false，都需要更新
        if (gift.getIsHot() != null) {
            updateWrapper.set(Gift::getIsHot, gift.getIsHot());
        }
        updateWrapper.set(Gift::getUpdatedAt, LocalDateTime.now());
        
        update(updateWrapper);
        return getById(gift.getId());
    }

    /**
     * 删除礼物
     */
    public void deleteGift(Long id) {
        removeById(id);
    }

    /**
     * 赠送礼物
     */
    @Transactional
    public GiftRecord sendGift(GiftRecord giftRecord) {
        // 获取礼物信息
        Gift gift = getById(giftRecord.getGiftId());
        if (gift == null) {
            throw new BusinessException(404, "礼物不存在");
        }
        // 计算总金额
        double totalAmount = gift.getPrice() * giftRecord.getCount();
        giftRecord.setTotalAmount(totalAmount);
        giftRecord.setCreatedAt(LocalDateTime.now());
        
        // 检查发送者余额
        UserBalance senderBalance = withdrawService.getOrCreateUserBalance(giftRecord.getFromUserId());
        if (senderBalance.getAvailableBalance().compareTo(BigDecimal.valueOf(totalAmount)) < 0) {
            throw new BusinessException(400, "余额不足");
        }
        
        // 扣减发送者余额
        senderBalance.setAvailableBalance(senderBalance.getAvailableBalance().subtract(BigDecimal.valueOf(totalAmount)));
        senderBalance.setUpdatedAt(LocalDateTime.now());
        userBalanceMapper.updateById(senderBalance);
        
        // 保存礼物记录
        giftRecordMapper.insert(giftRecord);
        
        // 为接收者增加收入
        if (giftRecord.getToUserId() != null && !giftRecord.getFromUserId().equals(giftRecord.getToUserId())) {
            withdrawService.addIncome(giftRecord.getToUserId(), BigDecimal.valueOf(totalAmount));
        }
        
        // 广播礼物信息
        broadcastGift(giftRecord, gift);
        return giftRecord;
    }

    /**
     * 获取直播间的礼物记录
     */
    public List<GiftRecord> getGiftRecordList(Long liveId, int limit) {
        LambdaQueryWrapper<GiftRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GiftRecord::getLiveId, liveId);
        wrapper.orderByDesc(GiftRecord::getCreatedAt);
        wrapper.last("LIMIT " + limit);
        return giftRecordMapper.selectList(wrapper);
    }

    /**
     * 获取用户的礼物记录
     */
    public List<GiftRecord> getGiftRecordByUserId(Long userId, int limit) {
        LambdaQueryWrapper<GiftRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GiftRecord::getFromUserId, userId);
        wrapper.orderByDesc(GiftRecord::getCreatedAt);
        wrapper.last("LIMIT " + limit);
        return giftRecordMapper.selectList(wrapper);
    }

    /**
     * 获取直播间礼物统计
     */
    public Map<String, Object> getLiveGiftStats(Long liveId) {
        LambdaQueryWrapper<GiftRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GiftRecord::getLiveId, liveId);
        List<GiftRecord> records = giftRecordMapper.selectList(wrapper);
        
        Map<String, Object> stats = new HashMap<>();
        
        double totalAmount = records.stream()
            .mapToDouble(GiftRecord::getTotalAmount)
            .sum();
        
        int totalCount = records.stream()
            .mapToInt(GiftRecord::getCount)
            .sum();
        
        long uniqueGifters = records.stream()
            .map(GiftRecord::getFromUserId)
            .distinct()
            .count();
        
        Map<Long, Long> giftCountMap = records.stream()
            .collect(Collectors.groupingBy(GiftRecord::getGiftId, Collectors.summingLong(r -> (long) r.getCount())));
        
        List<Map<String, Object>> giftRanking = giftCountMap.entrySet().stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
            .limit(10)
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                Gift gift = getById(entry.getKey());
                item.put("giftId", entry.getKey());
                item.put("giftName", gift != null ? gift.getName() : "未知礼物");
                item.put("giftIcon", gift != null ? gift.getIcon() : "");
                item.put("count", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
        
        Map<Long, Double> userAmountMap = records.stream()
            .collect(Collectors.groupingBy(GiftRecord::getFromUserId, Collectors.summingDouble(GiftRecord::getTotalAmount)));
        
        List<Map<String, Object>> userRanking = userAmountMap.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(10)
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                User user = userService.getById(entry.getKey());
                item.put("userId", entry.getKey());
                item.put("username", user != null ? user.getUsername() : "匿名用户");
                item.put("nickname", user != null ? user.getNickname() : "匿名用户");
                item.put("avatar", user != null ? user.getAvatar() : "");
                item.put("totalAmount", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
        
        stats.put("totalAmount", totalAmount);
        stats.put("totalCount", totalCount);
        stats.put("uniqueGifters", uniqueGifters);
        stats.put("giftRanking", giftRanking);
        stats.put("userRanking", userRanking);
        
        return stats;
    }

    /**
     * 获取主播礼物统计
     */
    public Map<String, Object> getAnchorGiftStats(Long anchorId) {
        LambdaQueryWrapper<GiftRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GiftRecord::getToUserId, anchorId);
        List<GiftRecord> records = giftRecordMapper.selectList(wrapper);
        
        Map<String, Object> stats = new HashMap<>();
        
        double totalAmount = records.stream()
            .mapToDouble(GiftRecord::getTotalAmount)
            .sum();
        
        int totalCount = records.stream()
            .mapToInt(GiftRecord::getCount)
            .sum();
        
        long uniqueGifters = records.stream()
            .map(GiftRecord::getFromUserId)
            .distinct()
            .count();
        
        stats.put("totalAmount", totalAmount);
        stats.put("totalCount", totalCount);
        stats.put("uniqueGifters", uniqueGifters);
        
        return stats;
    }

    /**
     * 广播礼物信息
     */
    private void broadcastGift(GiftRecord giftRecord, Gift gift) {
        String channel = "gift:live:" + giftRecord.getLiveId();
        GiftMessage giftMessage = new GiftMessage();
        giftMessage.setGiftRecord(giftRecord);
        giftMessage.setGift(gift);
        
        // 尝试通过Redis发布消息
        try {
            redisTemplate.convertAndSend(channel, giftMessage);
        } catch (Exception e) {
            // Redis连接失败时，只记录日志，不影响正常功能
            e.printStackTrace();
        }
        
        // 尝试通过WebSocket广播
        try {
            User user = userService.getById(giftRecord.getFromUserId());
            String username = user != null ? user.getUsername() : "匿名用户";
            webSocketController.broadcastGift(
                giftRecord.getLiveId(), 
                giftRecord, 
                username, 
                gift.getName(), 
                gift.getIcon()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 礼物消息类
     */
    private static class GiftMessage {
        private GiftRecord giftRecord;
        private Gift gift;

        public GiftRecord getGiftRecord() {
            return giftRecord;
        }

        public void setGiftRecord(GiftRecord giftRecord) {
            this.giftRecord = giftRecord;
        }

        public Gift getGift() {
            return gift;
        }

        public void setGift(Gift gift) {
            this.gift = gift;
        }
    }
}