package com.zhibo.backend.controller;

import com.zhibo.backend.common.Result;
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

    @GetMapping("/overview")
    public Result<Map<String, Object>> getPlatformOverview() {
        Map<String, Object> data = analyticsService.getPlatformOverview();
        return Result.success("获取概览数据成功", data);
    }

    @GetMapping("/user-growth")
    public Result<List<Map<String, Object>>> getUserGrowthTrend(
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> data = analyticsService.getUserGrowthTrend(days);
        return Result.success(data);
    }

    @GetMapping("/live-ranking")
    public Result<List<Map<String, Object>>> getLiveRanking(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> data = analyticsService.getLiveRanking(limit);
        return Result.success(data);
    }

    @GetMapping("/gift-ranking")
    public Result<List<Map<String, Object>>> getGiftRanking(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> data = analyticsService.getGiftRanking(limit);
        return Result.success(data);
    }

    @GetMapping("/category-statistics")
    public Result<List<Map<String, Object>>> getCategoryStatistics() {
        List<Map<String, Object>> data = analyticsService.getCategoryStatistics();
        return Result.success(data);
    }

    @GetMapping("/user-activity")
    public Result<Map<String, Object>> getUserActivityStatistics() {
        Map<String, Object> data = analyticsService.getUserActivityStatistics();
        return Result.success(data);
    }

    @GetMapping("/live-trend")
    public Result<List<Map<String, Object>>> getLiveTrend(
            @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> data = analyticsService.getLiveTrend(days);
        return Result.success("获取直播趋势数据成功", data);
    }

    @GetMapping("/user-trend")
    public Result<List<Map<String, Object>>> getUserTrend(
            @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> data = analyticsService.getUserTrend(days);
        return Result.success("获取用户趋势数据成功", data);
    }

    @GetMapping("/gift-income")
    public Result<List<Map<String, Object>>> getGiftIncome(
            @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> data = analyticsService.getGiftIncome(days);
        return Result.success("获取礼物收入数据成功", data);
    }

    @GetMapping("/hot-lives")
    public Result<List<Map<String, Object>>> getHotLives(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> data = analyticsService.getHotLives(limit);
        return Result.success("获取热门直播数据成功", data);
    }

    @GetMapping("/hot-anchors")
    public Result<List<Map<String, Object>>> getHotAnchors(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> data = analyticsService.getHotAnchors(limit);
        return Result.success("获取热门主播数据成功", data);
    }
}
