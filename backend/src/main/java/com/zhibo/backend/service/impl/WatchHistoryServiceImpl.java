package com.zhibo.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.Live;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.entity.WatchHistory;
import com.zhibo.backend.mapper.WatchHistoryMapper;
import com.zhibo.backend.service.LiveService;
import com.zhibo.backend.service.UserService;
import com.zhibo.backend.service.WatchHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WatchHistoryServiceImpl extends ServiceImpl<WatchHistoryMapper, WatchHistory> implements WatchHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(WatchHistoryServiceImpl.class);

    @Autowired
    private LiveService liveService;

    @Autowired
    private UserService userService;

    @Override
    public void recordWatchHistory(Long userId, Long liveId, Integer watchDuration) {
        logger.info("Recording watch history - userId: {}, liveId: {}, duration: {}", userId, liveId, watchDuration);
        
        Live live = liveService.getById(liveId);
        if (live == null) {
            logger.warn("Live not found - liveId: {}", liveId);
            return;
        }

        User anchor = userService.getById(live.getUserId());
        String anchorName = anchor != null ? anchor.getNickname() : "未知主播";

        LambdaQueryWrapper<WatchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WatchHistory::getUserId, userId)
               .eq(WatchHistory::getLiveId, liveId);
        
        WatchHistory history = getOne(wrapper);
        
        if (history != null) {
            history.setWatchDuration(history.getWatchDuration() + watchDuration);
            history.setLiveTitle(live.getTitle());
            history.setLiveCover(live.getCover());
            history.setAnchorName(anchorName);
            updateById(history);
            logger.info("Updated watch history - id: {}", history.getId());
        } else {
            history = new WatchHistory();
            history.setUserId(userId);
            history.setLiveId(liveId);
            history.setLiveTitle(live.getTitle());
            history.setLiveCover(live.getCover());
            history.setAnchorId(live.getUserId());
            history.setAnchorName(anchorName);
            history.setWatchDuration(watchDuration);
            save(history);
            logger.info("Created new watch history - id: {}", history.getId());
        }
    }

    @Override
    public Page<WatchHistory> getUserWatchHistory(Long userId, int page, int size) {
        logger.info("Getting user watch history - userId: {}, page: {}, size: {}", userId, page, size);
        
        LambdaQueryWrapper<WatchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WatchHistory::getUserId, userId)
               .orderByDesc(WatchHistory::getUpdatedAt);
        
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public void clearUserWatchHistory(Long userId) {
        logger.info("Clearing user watch history - userId: {}", userId);
        
        LambdaQueryWrapper<WatchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WatchHistory::getUserId, userId);
        
        remove(wrapper);
    }

    @Override
    public void deleteWatchHistory(Long userId, Long historyId) {
        logger.info("Deleting watch history - userId: {}, historyId: {}", userId, historyId);
        
        LambdaQueryWrapper<WatchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WatchHistory::getId, historyId)
               .eq(WatchHistory::getUserId, userId);
        
        remove(wrapper);
    }
}
