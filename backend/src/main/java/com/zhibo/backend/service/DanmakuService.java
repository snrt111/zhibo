package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.controller.WebSocketController;
import com.zhibo.backend.entity.Danmaku;
import com.zhibo.backend.entity.DanmakuVO;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.mapper.DanmakuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DanmakuService extends ServiceImpl<DanmakuMapper, Danmaku> {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private UserService userService;

    @Autowired
    private SensitiveWordService sensitiveWordService;
    
    @Autowired
    private ContentAuditService contentAuditService;

    // 存储直播间的WebSocket连接数
    private ConcurrentHashMap<Long, Integer> liveConnections = new ConcurrentHashMap<>();

    /**
     * 发送弹幕
     */
    public Danmaku sendDanmaku(Danmaku danmaku) {
        if (danmaku.getContent() == null || danmaku.getContent().trim().isEmpty()) {
            throw new RuntimeException("弹幕内容不能为空");
        }
        
        String originalContent = danmaku.getContent().trim();
        
        ContentAuditService.AuditResult auditResult = contentAuditService.checkContent(originalContent);
        
        if (auditResult.shouldBlock) {
            throw new RuntimeException("弹幕内容包含违规信息，无法发送");
        }
        
        String filteredContent = originalContent;
        if (auditResult.shouldFilter) {
            filteredContent = sensitiveWordService.filterSensitiveWords(originalContent);
        }
        danmaku.setContent(filteredContent);
        
        if (danmaku.getColor() == null) {
            danmaku.setColor("#FFFFFF");
        }
        if (danmaku.getFontSize() == null) {
            danmaku.setFontSize(1);
        }
        danmaku.setCreatedAt(LocalDateTime.now());
        save(danmaku);
        
        contentAuditService.saveAuditRecord(4, danmaku.getId(), originalContent, auditResult);
        
        broadcastDanmaku(danmaku);
        return danmaku;
    }

    /**
     * 获取直播间的弹幕列表（包含用户名）
     */
    public List<DanmakuVO> getDanmakuList(Long liveId, int limit) {
        LambdaQueryWrapper<Danmaku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Danmaku::getLiveId, liveId);
        wrapper.orderByDesc(Danmaku::getCreatedAt);
        wrapper.last("LIMIT " + limit);
        List<Danmaku> danmakuList = list(wrapper);
        
        List<Long> userIds = danmakuList.stream()
            .map(Danmaku::getUserId)
            .distinct()
            .collect(Collectors.toList());
        
        Map<Long, String> usernameMap = userService.listByIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
        
        return danmakuList.stream()
            .map(danmaku -> {
                String username = usernameMap.getOrDefault(danmaku.getUserId(), "匿名用户");
                return DanmakuVO.fromDanmaku(danmaku, username);
            })
            .collect(Collectors.toList());
    }

    /**
     * 广播弹幕
     */
    private void broadcastDanmaku(Danmaku danmaku) {
        String channel = "danmaku:live:" + danmaku.getLiveId();
        
        // 尝试通过Redis发布消息
        try {
            redisTemplate.convertAndSend(channel, danmaku);
        } catch (Exception e) {
            // Redis连接失败时，只记录日志，不影响正常功能
            e.printStackTrace();
        }
        
        // 尝试通过WebSocket广播
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