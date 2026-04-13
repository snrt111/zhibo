package com.zhibo.backend.controller;

import com.zhibo.backend.service.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/realtime")
    public ResponseEntity<?> getRealtimeMetrics() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取实时监控数据成功");
        response.put("timestamp", System.currentTimeMillis());
        
        Map<String, Object> data = new HashMap<>();
        data.put("onlineUserCount", metricsService.getOnlineUserCount());
        data.put("liveStreamCount", metricsService.getLiveStreamCount());
        data.put("danmakuCount", metricsService.getDanmakuCount());
        data.put("giftCount", metricsService.getGiftCount());
        
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}
