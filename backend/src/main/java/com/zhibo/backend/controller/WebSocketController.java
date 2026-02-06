package com.zhibo.backend.controller;

import com.zhibo.backend.entity.Danmaku;
import com.zhibo.backend.entity.GiftRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/danmaku/{liveId}")
    public void handleDanmaku(@DestinationVariable Long liveId, @Payload DanmakuMessage message) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "danmaku");
        response.put("userId", message.getUserId());
        response.put("username", message.getUsername());
        response.put("content", message.getContent());
        response.put("color", message.getColor());
        response.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/live/" + liveId, response);
    }

    @MessageMapping("/gift/{liveId}")
    public void handleGift(@DestinationVariable Long liveId, @Payload GiftMessage message) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "gift");
        response.put("fromUserId", message.getFromUserId());
        response.put("fromUsername", message.getFromUsername());
        response.put("giftId", message.getGiftId());
        response.put("giftName", message.getGiftName());
        response.put("giftImage", message.getGiftImage());
        response.put("giftCount", message.getGiftCount());
        response.put("totalPrice", message.getTotalPrice());
        response.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/live/" + liveId, response);
    }

    public void broadcastDanmaku(Long liveId, Danmaku danmaku, String username) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "danmaku");
        response.put("userId", danmaku.getUserId());
        response.put("username", username);
        response.put("content", danmaku.getContent());
        response.put("color", danmaku.getColor());
        response.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/live/" + liveId, response);
    }

    public void broadcastGift(Long liveId, GiftRecord giftRecord, String fromUsername, String giftName, String giftImage) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "gift");
        response.put("fromUserId", giftRecord.getFromUserId());
        response.put("fromUsername", fromUsername);
        response.put("giftId", giftRecord.getGiftId());
        response.put("giftName", giftName);
        response.put("giftImage", giftImage);
        response.put("giftCount", giftRecord.getCount());
        response.put("totalPrice", giftRecord.getTotalAmount());
        response.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/live/" + liveId, response);
    }

    public static class DanmakuMessage {
        private Long userId;
        private String username;
        private String content;
        private String color;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public static class GiftMessage {
        private Long fromUserId;
        private String fromUsername;
        private Long giftId;
        private String giftName;
        private String giftImage;
        private Integer giftCount;
        private Integer totalPrice;

        public Long getFromUserId() { return fromUserId; }
        public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
        public String getFromUsername() { return fromUsername; }
        public void setFromUsername(String fromUsername) { this.fromUsername = fromUsername; }
        public Long getGiftId() { return giftId; }
        public void setGiftId(Long giftId) { this.giftId = giftId; }
        public String getGiftName() { return giftName; }
        public void setGiftName(String giftName) { this.giftName = giftName; }
        public String getGiftImage() { return giftImage; }
        public void setGiftImage(String giftImage) { this.giftImage = giftImage; }
        public Integer getGiftCount() { return giftCount; }
        public void setGiftCount(Integer giftCount) { this.giftCount = giftCount; }
        public Integer getTotalPrice() { return totalPrice; }
        public void setTotalPrice(Integer totalPrice) { this.totalPrice = totalPrice; }
    }
}
