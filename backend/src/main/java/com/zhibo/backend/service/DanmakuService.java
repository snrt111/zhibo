package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.controller.WebSocketController;
import com.zhibo.backend.entity.Danmaku;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.mapper.DanmakuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DanmakuService extends ServiceImpl<DanmakuMapper, Danmaku> {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private UserService userService;

    // 存储直播间的WebSocket连接数
    private ConcurrentHashMap<Long, Integer> liveConnections = new ConcurrentHashMap<>();

    /**
     * 发送弹幕
     */
    public Danmaku sendDanmaku(Danmaku danmaku) {
        // 设置默认值
        if (danmaku.getColor() == null) {
            danmaku.setColor("#FFFFFF");
        }
        danmaku.setCreatedAt(LocalDateTime.now());
        // 保存弹幕
        save(danmaku);
        // 广播弹幕到直播间
        broadcastDanmaku(danmaku);
        return danmaku;
    }

    /**
     * 获取直播间的弹幕列表
     */
    public List<Danmaku> getDanmakuList(Long liveId, int limit) {
        LambdaQueryWrapper<Danmaku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Danmaku::getLiveId, liveId);
        wrapper.orderByDesc(Danmaku::getCreatedAt);
        wrapper.last("LIMIT " + limit);
        return list(wrapper);
    }

    /**
     * 广播弹幕
     */
    private void broadcastDanmaku(Danmaku danmaku) {
        String channel = "danmaku:live:" + danmaku.getLiveId();
        redisTemplate.convertAndSend(channel, danmaku);
        
        try {
            User user = userService.getById(danmaku.getUserId());
            String username = user != null ? user.getUsername() : "匿名用户";
            webSocketController.broadcastDanmaku(danmaku.getLiveId(), danmaku, username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加直播间连接数
     */
    public void addConnection(Long liveId) {
        liveConnections.compute(liveId, (k, v) -> v == null ? 1 : v + 1);
    }

    /**
     * 减少直播间连接数
     */
    public void removeConnection(Long liveId) {
        liveConnections.compute(liveId, (k, v) -> v == null || v <= 1 ? 0 : v - 1);
    }

    /**
     * 获取直播间连接数
     */
    public int getConnectionCount(Long liveId) {
        return liveConnections.getOrDefault(liveId, 0);
    }
}