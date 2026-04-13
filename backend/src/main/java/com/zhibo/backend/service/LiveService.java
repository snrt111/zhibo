package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.Live;
import com.zhibo.backend.exception.BusinessException;
import com.zhibo.backend.mapper.LiveMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LiveService extends ServiceImpl<LiveMapper, Live> {

    @Autowired
    private StreamService streamService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 创建直播
     */
    public Live createLive(Live live) {
        // 生成唯一的推流密钥
        String streamKey = UUID.randomUUID().toString().replace("-", "");
        live.setStreamKey(streamKey);
        // 设置默认值
        live.setStatus(0); // 初始状态为未开始
        live.setViewCount(0L); // 初始观看人数为0
        // 保存直播
        save(live);
        return live;
    }

    /**
     * 开始直播
     */
    public boolean startLive(Long liveId) {
        Live live = getById(liveId);
        if (live == null) {
            throw new BusinessException(404, "直播不存在");
        }
        if (live.getStatus() != 0) {
            throw new BusinessException(400, "直播状态不正确，只能开始未开始的直播");
        }
        // 更新直播状态
        live.setStatus(1); // 设置为直播中
        live.setStartTime(LocalDateTime.now());
        boolean updated = updateById(live);
        
        // 开始转码直播流
        if (updated) {
            streamService.startStreaming(live.getStreamKey());
            // 发送开播提醒给粉丝
            notificationService.sendLiveStartNotification(live);
        }
        return updated;
    }

    /**
     * 结束直播
     */
    public boolean endLive(Long liveId) {
        Live live = getById(liveId);
        if (live == null) {
            throw new BusinessException(404, "直播不存在");
        }
        if (live.getStatus() != 1) {
            throw new BusinessException(400, "直播状态不正确，只能结束进行中的直播");
        }
        // 停止转码直播流
        streamService.stopStreaming(live.getStreamKey());
        
        // 更新直播状态
        live.setStatus(2); // 设置为已结束
        live.setEndTime(LocalDateTime.now());
        return updateById(live);
    }

    /**
     * 获取直播列表
     */
    public List<Live> getLiveList(Integer status) {
        LambdaQueryWrapper<Live> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Live::getStatus, status);
        }
        wrapper.orderByDesc(Live::getCreatedAt);
        return list(wrapper);
    }

    /**
     * 获取管理员直播列表（分页）
     */
    public Page<Map<String, Object>> getAdminLiveList(Integer status, Long categoryId, String keyword, int page, int size) {
        Page<Live> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Live> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Live::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(Live::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Live::getTitle, keyword);
        }
        
        wrapper.orderByDesc(Live::getCreatedAt);
        Page<Live> livePage = page(pageParam, wrapper);
        
        Page<Map<String, Object>> resultPage = new Page<>(livePage.getCurrent(), livePage.getSize(), livePage.getTotal());
        List<Map<String, Object>> records = livePage.getRecords().stream()
                .map(this::convertLiveToMap)
                .collect(java.util.stream.Collectors.toList());
        resultPage.setRecords(records);
        
        return resultPage;
    }

    private Map<String, Object> convertLiveToMap(Live live) {
        Map<String, Object> liveData = new HashMap<>();
        liveData.put("id", live.getId());
        liveData.put("userId", live.getUserId());
        liveData.put("title", live.getTitle());
        liveData.put("description", live.getDescription());
        liveData.put("cover", live.getCover());
        liveData.put("categoryId", live.getCategoryId());
        liveData.put("streamKey", live.getStreamKey());
        liveData.put("status", live.getStatus());
        liveData.put("viewCount", live.getViewCount());
        liveData.put("startTime", live.getStartTime());
        liveData.put("endTime", live.getEndTime());
        liveData.put("createdAt", live.getCreatedAt());
        liveData.put("updatedAt", live.getUpdatedAt());
        return liveData;
    }

    /**
     * 根据用户ID获取直播列表
     */
    public List<Live> getLiveListByUserId(Long userId) {
        LambdaQueryWrapper<Live> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Live::getUserId, userId);
        wrapper.orderByDesc(Live::getCreatedAt);
        return list(wrapper);
    }

    /**
     * 根据分类ID获取直播列表
     */
    public List<Live> getLiveListByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Live> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Live::getCategoryId, categoryId);
        wrapper.orderByDesc(Live::getCreatedAt);
        return list(wrapper);
    }

    /**
     * 增加观看人数
     */
    public boolean increaseViewCount(Long liveId) {
        Live live = getById(liveId);
        if (live == null) {
            return false;
        }
        live.setViewCount(live.getViewCount() + 1);
        return updateById(live);
    }

    /**
     * 根据推流密钥获取直播
     */
    public Live getByStreamKey(String streamKey) {
        LambdaQueryWrapper<Live> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Live::getStreamKey, streamKey);
        return getOne(wrapper);
    }

    /**
     * 获取直播推流地址
     */
    public String getPushUrl(Long liveId) {
        Live live = getById(liveId);
        if (live == null) {
            throw new RuntimeException("直播不存在");
        }
        return streamService.getPushUrl(live.getStreamKey());
    }

    /**
     * 获取直播播放地址
     */
    public String getPlayUrl(Long liveId) {
        Live live = getById(liveId);
        if (live == null) {
            throw new RuntimeException("直播不存在");
        }
        return streamService.getPlayUrl(live.getStreamKey());
    }

    /**
     * 删除直播
     */
    public boolean deleteLive(Long liveId) {
        Live live = getById(liveId);
        if (live == null) {
            throw new RuntimeException("直播不存在");
        }
        if (live.getStatus() == 1) {
            throw new RuntimeException("直播正在进行中，无法删除");
        }
        return removeById(liveId);
    }

    /**
     * 批量结束直播
     */
    public int batchEndLive(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            Live live = getById(id);
            if (live != null && live.getStatus() == 1) {
                live.setStatus(2);
                live.setEndTime(LocalDateTime.now());
                updateById(live);
                count++;
            }
        }
        return count;
    }

    /**
     * 批量删除直播
     */
    public int batchDeleteLive(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            try {
                if (deleteLive(id)) {
                    count++;
                }
            } catch (Exception e) {
                // 忽略单个删除失败的情况
            }
        }
        return count;
    }
}