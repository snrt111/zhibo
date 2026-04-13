package com.zhibo.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.WatchHistory;
import com.zhibo.backend.exception.BusinessException;
import com.zhibo.backend.service.WatchHistoryService;
import com.zhibo.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/watch-history")
public class WatchHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(WatchHistoryController.class);

    @Autowired
    private WatchHistoryService watchHistoryService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/record")
    public Result<Void> recordWatchHistory(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        logger.info("Received request: {}", request);
        
        Object liveIdObj = request.get("liveId");
        Object watchDurationObj = request.get("watchDuration");
        
        logger.info("liveId from request: {}, type: {}", liveIdObj, liveIdObj != null ? liveIdObj.getClass() : "null");
        logger.info("watchDuration from request: {}, type: {}", watchDurationObj, watchDurationObj != null ? watchDurationObj.getClass() : "null");
        
        if (liveIdObj == null) {
            logger.error("liveId is null in request");
            throw new BusinessException("直播ID不能为空");
        }
        
        Long liveId = Long.valueOf(liveIdObj.toString());
        Integer watchDuration = Integer.valueOf(watchDurationObj.toString());
        
        logger.info("Recording watch history - userId: {}, liveId: {}, duration: {}", userId, liveId, watchDuration);
        
        watchHistoryService.recordWatchHistory(userId, liveId, watchDuration);
        return Result.success("观看记录已保存", null);
    }

    @GetMapping("/list")
    public Result<Page<WatchHistory>> getUserWatchHistory(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        logger.info("Getting watch history - userId: {}, page: {}, size: {}", userId, page, size);
        
        Page<WatchHistory> result = watchHistoryService.getUserWatchHistory(userId, page, size);
        return Result.success("获取成功", result);
    }

    @DeleteMapping("/clear")
    public Result<Void> clearUserWatchHistory(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        logger.info("Clearing watch history - userId: {}", userId);
        
        watchHistoryService.clearUserWatchHistory(userId);
        return Result.success("观看历史已清空", null);
    }

    @DeleteMapping("/delete/{historyId}")
    public Result<Void> deleteWatchHistory(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long historyId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        logger.info("Deleting watch history - userId: {}, historyId: {}", userId, historyId);
        
        watchHistoryService.deleteWatchHistory(userId, historyId);
        return Result.success("记录已删除", null);
    }
}
