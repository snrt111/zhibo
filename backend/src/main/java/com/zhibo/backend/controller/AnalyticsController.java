package com.zhibo.backend.controller;

import com.zhibo.backend.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * 获取平台数据总览
     */
    @GetMapping("/overview")
    public Map<String, Object> getPlatformOverview() {
        return analyticsService.getPlatformOverview();
    }

    /**
     * 获取用户增长趋势
     */
    @GetMapping("/user-growth")
    public List<Map<String, Object>> getUserGrowthTrend(
            @RequestParam(defaultValue = "30") int days) {
        return analyticsService.getUserGrowthTrend(days);
    }

    /**
     * 获取直播热度排行
     */
    @GetMapping("/live-ranking")
    public List<Map<String, Object>> getLiveRanking(
            @RequestParam(defaultValue = "10") int limit) {
        return analyticsService.getLiveRanking(limit);
    }

    /**
     * 获取礼物赠送排行
     */
    @GetMapping("/gift-ranking")
    public List<Map<String, Object>> getGiftRanking(
            @RequestParam(defaultValue = "10") int limit) {
        return analyticsService.getGiftRanking(limit);
    }

    /**
     * 获取分类直播数量统计
     */
    @GetMapping("/category-statistics")
    public List<Map<String, Object>> getCategoryStatistics() {
        return analyticsService.getCategoryStatistics();
    }

    /**
     * 获取用户活跃度统计
     */
    @GetMapping("/user-activity")
    public Map<String, Object> getUserActivityStatistics() {
        return analyticsService.getUserActivityStatistics();
    }
}
