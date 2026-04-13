package com.zhibo.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AnalyticsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取平台数据总览
     */
    public Map<String, Object> getPlatformOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 获取总用户数
        Integer totalUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user", Integer.class
        );
        overview.put("totalUserCount", totalUsers);
        
        // 获取总直播数
        Integer totalLives = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM live", Integer.class
        );
        overview.put("totalLiveCount", totalLives);
        
        // 获取总观看人数
        Long totalViews = jdbcTemplate.queryForObject(
            "SELECT SUM(view_count) FROM live", Long.class
        );
        overview.put("totalViewCount", totalViews != null ? totalViews : 0);
        
        // 获取总礼物金额
        Double totalGiftAmount = jdbcTemplate.queryForObject(
            "SELECT SUM(total_amount) FROM gift_record", Double.class
        );
        overview.put("totalGiftIncome", totalGiftAmount != null ? totalGiftAmount : 0);
        
        // 获取热门直播（按观看数排序，取前5）
        List<Map<String, Object>> hotLives = jdbcTemplate.queryForList(
            "SELECT l.title, l.view_count as viewCount " +
            "FROM live l " +
            "ORDER BY l.view_count DESC " +
            "LIMIT 5"
        );
        overview.put("hotLives", hotLives);
        
        // 获取热门主播（按直播数量排序，取前5）
        List<Map<String, Object>> hotAnchors = jdbcTemplate.queryForList(
            "SELECT u.username, COUNT(l.id) as liveCount " +
            "FROM user u " +
            "LEFT JOIN live l ON u.id = l.user_id " +
            "GROUP BY u.id, u.username " +
            "ORDER BY liveCount DESC " +
            "LIMIT 5"
        );
        overview.put("hotAnchors", hotAnchors);
        
        return overview;
    }

    /**
     * 获取用户增长趋势
     */
    public List<Map<String, Object>> getUserGrowthTrend(int days) {
        String sql = String.format(
            "SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM user " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL %d DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date", days
        );
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取直播热度排行
     */
    public List<Map<String, Object>> getLiveRanking(int limit) {
        String sql = String.format(
            "SELECT l.id, l.title, l.user_id, l.view_count, u.nickname as user_nickname " +
            "FROM live l " +
            "JOIN user u ON l.user_id = u.id " +
            "ORDER BY l.view_count DESC " +
            "LIMIT %d", limit
        );
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取礼物赠送排行
     */
    public List<Map<String, Object>> getGiftRanking(int limit) {
        String sql = String.format(
            "SELECT g.id, g.name, COUNT(gr.id) as count, SUM(gr.total_amount) as total_amount " +
            "FROM gift g " +
            "LEFT JOIN gift_record gr ON g.id = gr.gift_id " +
            "GROUP BY g.id, g.name " +
            "ORDER BY count DESC " +
            "LIMIT %d", limit
        );
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取分类直播数量统计
     */
    public List<Map<String, Object>> getCategoryStatistics() {
        String sql = "SELECT c.id, c.name, COUNT(l.id) as count " +
                     "FROM category c " +
                     "LEFT JOIN live l ON c.id = l.category_id " +
                     "GROUP BY c.id, c.name " +
                     "ORDER BY count DESC";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取用户活跃度统计
     */
    public Map<String, Object> getUserActivityStatistics() {
        Map<String, Object> activity = new HashMap<>();
        
        // 获取活跃用户数（近7天有登录记录的用户）
        Integer activeUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT user_id) FROM danmaku WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)", 
            Integer.class
        );
        activity.put("activeUsers", activeUsers != null ? activeUsers : 0);
        
        // 获取平均观看时长（模拟数据）
        activity.put("averageWatchTime", 35.5); // 分钟
        
        return activity;
    }

    /**
     * 获取直播趋势数据
     */
    public List<Map<String, Object>> getLiveTrend(int days) {
        String sql = String.format(
            "SELECT DATE(created_at) as date, COUNT(*) as count, SUM(view_count) as viewCount " +
            "FROM live " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL %d DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date", days
        );
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取用户趋势数据
     */
    public List<Map<String, Object>> getUserTrend(int days) {
        String sql = String.format(
            "SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM user " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL %d DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date", days
        );
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取礼物收入趋势数据
     */
    public List<Map<String, Object>> getGiftIncome(int days) {
        String sql = String.format(
            "SELECT DATE(created_at) as date, COUNT(*) as count, SUM(total_amount) as amount " +
            "FROM gift_record " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL %d DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date", days
        );
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取热门直播列表
     */
    public List<Map<String, Object>> getHotLives(int limit) {
        String sql = String.format(
            "SELECT l.id, l.title, l.view_count as viewCount, u.username as anchorName " +
            "FROM live l " +
            "JOIN user u ON l.user_id = u.id " +
            "ORDER BY l.view_count DESC " +
            "LIMIT %d", limit
        );
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取热门主播列表
     */
    public List<Map<String, Object>> getHotAnchors(int limit) {
        String sql = String.format(
            "SELECT u.id, u.username, u.nickname, " +
            "COUNT(l.id) as liveCount, " +
            "COALESCE(SUM(l.view_count), 0) as totalViews, " +
            "COALESCE(SUM(gr.total_amount), 0) as totalIncome " +
            "FROM user u " +
            "LEFT JOIN live l ON u.id = l.user_id " +
            "LEFT JOIN gift_record gr ON l.id = gr.live_id " +
            "GROUP BY u.id, u.username, u.nickname " +
            "ORDER BY liveCount DESC " +
            "LIMIT %d", limit
        );
        return jdbcTemplate.queryForList(sql);
    }
}