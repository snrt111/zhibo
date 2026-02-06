package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.controller.WebSocketController;
import com.zhibo.backend.entity.Gift;
import com.zhibo.backend.entity.GiftRecord;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.mapper.GiftMapper;
import com.zhibo.backend.mapper.GiftRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 获取所有礼物列表
     */
    public List<Gift> getGiftList() {
        return list();
    }

    /**
     * 根据ID获取礼物
     */
    public Gift getGiftById(Long id) {
        return getById(id);
    }

    /**
     * 赠送礼物
     */
    public GiftRecord sendGift(GiftRecord giftRecord) {
        // 获取礼物信息
        Gift gift = getById(giftRecord.getGiftId());
        if (gift == null) {
            throw new RuntimeException("礼物不存在");
        }
        // 计算总金额
        double totalAmount = gift.getPrice() * giftRecord.getCount();
        giftRecord.setTotalAmount(totalAmount);
        giftRecord.setCreatedAt(LocalDateTime.now());
        // 保存礼物记录
        giftRecordMapper.insert(giftRecord);
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
     * 广播礼物信息
     */
    private void broadcastGift(GiftRecord giftRecord, Gift gift) {
        String channel = "gift:live:" + giftRecord.getLiveId();
        GiftMessage giftMessage = new GiftMessage();
        giftMessage.setGiftRecord(giftRecord);
        giftMessage.setGift(gift);
        redisTemplate.convertAndSend(channel, giftMessage);
        
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