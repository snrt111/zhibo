package com.zhibo.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketBroadcastService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastDanmaku(Long liveId, Map<String, Object> danmakuData) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "danmaku");
        response.putAll(danmakuData);
        response.put("timestamp", System.currentTimeMillis());
        
        System.out.println("广播弹幕到 /topic/live/" + liveId + ", 消息: " + response);
        messagingTemplate.convertAndSend("/topic/live/" + liveId, response);
    }

    public void broadcastGift(Long liveId, Map<String, Object> giftData) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "gift");
        response.putAll(giftData);
        response.put("timestamp", System.currentTimeMillis());
        
        System.out.println("广播礼物到 /topic/live/" + liveId + ", 消息: " + response);
        messagingTemplate.convertAndSend("/topic/live/" + liveId, response);
    }

    public void broadcastComment(Long liveId, Map<String, Object> commentData) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "comment");
        response.putAll(commentData);
        response.put("timestamp", System.currentTimeMillis());
        
        System.out.println("广播评论到 /topic/live/" + liveId + ", 消息: " + response);
        messagingTemplate.convertAndSend("/topic/live/" + liveId, response);
    }

    /**
     * 向指定用户发送通知消息
     *
     * @param userId  用户ID
     * @param message 通知内容
     */
    public void sendNotificationToUser(Long userId, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "notification");
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        
        System.out.println("发送通知到用户 " + userId + ", 消息: " + response);
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/notifications", response);
    }
}
