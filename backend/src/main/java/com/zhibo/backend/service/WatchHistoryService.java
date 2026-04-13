package com.zhibo.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhibo.backend.entity.WatchHistory;

public interface WatchHistoryService extends IService<WatchHistory> {
    
    void recordWatchHistory(Long userId, Long liveId, Integer watchDuration);
    
    Page<WatchHistory> getUserWatchHistory(Long userId, int page, int size);
    
    void clearUserWatchHistory(Long userId);
    
    void deleteWatchHistory(Long userId, Long historyId);
}
